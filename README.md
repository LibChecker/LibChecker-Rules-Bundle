# LibChecker-Rules-Bundle

[![](https://jitpack.io/v/LibChecker/LibChecker-Rules-Bundle.svg)](https://jitpack.io/v/LibChecker/LibChecker-Rules-Bundle.svg)

Make it easy to use [LibChecker](https://github.com/LibChecker/LibChecker) marked libraries rules in your apps.

## Download

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
dependencies {
    implementation "com.github.LibChecker:LibChecker-Rules-Bundle:${latest_version}"
}
```

## Quick Tutorial

Initialize the v5 baseline directly from the AAR:
```kotlin
LCRules.init(context)
// Select the existing cloud detail repository; icons come from Bundle drawables.
LCRules.setRemoteRepo(LCRemoteRepo.GitHub)
```

Get marked rule in a suspend context
```kotlin
val rule: Rule? = LCRules.getRule(libName = "libflutter.so", type = NATIVE, useRegex = false)

val activityRule: Rule? = LCRules.getRule(
    libName = "androidx.compose.ui.tooling.PreviewActivity",
    type = ACTIVITY,
    useRegex = false
)

val regexRule: Rule? = LCRules.getRule(
    libName = "libAMapSDK_MAP_v7_9_1.so",
    type = NATIVE,
    useRegex = true
)
```

The SDK reads v5 data through Android's SQLite APIs. The AAR carries exactly one
baseline database and its metadata, with no v4 assets, descriptions or SVGs. It does
not require Room, AppCompat, Core KTX, or kotlinx-coroutines in host apps.
Choose the coroutine context for `getRule(...)` in your app.

## Migration

- Remove Room, AppCompat, and Core KTX dependencies if they were added only for
  this package.
- Call `LCRules.init(context)` to validate, repair and select the AAR baseline.
  It works independently, without an App database path or a second bundled copy.
- The app owns downloaded current/previous versions. After validating a candidate
  newer than the baseline, call `LCRules.activateDatabase(file)` to switch. A failed
  activation preserves the open baseline. There is no v4 fallback.
- `RuleReader.open` accepts only schema 5 and requires its sibling metadata.json.
  The removed `getRulesAssetPath()` API no longer refers to any shipped asset.
- Use `LCRemoteRepo.GitHub` / `LCRemoteRepo.GitLab`. The old `Github` /
  `Gitlab` aliases still work, but are deprecated.
- Use `LCRules.close()` to release the active reader. Removed unused APIs include
  `closeDb`, `getVersion`, `getItemCounts`, `setLocale`, `readDetail` and `resolveAsset`.
- Do not use the old internal database classes (`RuleDao`, `RuleDatabase`,
  `RuleRepository`, `Repositories`, `RuleEntity`, or `IAPI`). Query rules only
  through `LCRules.getRule(...)`.

## JitPack

JitPack builds with JDK 17 and publishes the library module through
`:library:publishToMavenLocal`.

## Bundled baseline and downloaded v5 data

`assets/lcrules/v5/rules.db` and `metadata.json` are the sole baseline data in the
AAR. `LCRules.init(context)` owns one persistent copy under
`noBackupFilesDir/lcrules-v5/bundled`. It compares both files with the packaged
assets, validates a staged copy before replacing a damaged cache, and recovers an
interrupted directory rename. Every call reselects the baseline, including when a
downloaded reader was previously active. Successful initialization removes temporary
staging/backup copies. The App must not install another copy of the baseline.

```kotlin
LCRules.init(context)
val baseline = LCRules.getMetadata() // dataVersion is Long
val database = LCRules.getDatabaseFile() // active, read-only path
// App Store checks archive hash, metadata, and candidate version > baseline.
RuleReader.open(downloadedDatabase).use { candidate ->
    check(candidate.metadata.dataVersion > baseline.dataVersion)
}
LCRules.activateDatabase(downloadedDatabase)
```

The app's existing download Store retains and validates current/previous versions;
it falls back to the already-open AAR baseline if none is newer and valid. Same
versions prefer the AAR baseline. Activation is distinct from Context initialization;
`init(File)` is removed. `getDatabaseFile()` always identifies the active reader.
The corrected, unpublished dataVersion 45 Android ZIP contains exactly `rules.db`
and `metadata.json`, with no library descriptions, SVGs, icon index or fixtures.

`RuleReader.open` opens read-only, checks SQLite `quick_check`, required schema
and row values, compiles every regex, rejects duplicate type/name pairs, checks
metadata identity/count/reader compatibility. Invalid candidates throw without changing the
current singleton. It never downloads, repairs or deletes an app-selected file.
The app validates archive sizes/hashes before opening it and keeps installed
directories immutable while selected.

The Android table has exactly `_id`, `name`, `label`, `type`, `iconIndex`,
`isRegexRule`, `regexName`, and `priority`. Expanded unpublished v5 previews are
rejected; canonical and portable data retain their full source metadata.
`Rule` contains its original seven fields. `iconRes` and `isSimpleColorIcon` come
from Bundle's `IconResMap` and drawables; unknown or web-only indexes use the
placeholder. `descriptionUrl` uses the type/name or regexName cloud path and the
selected GitHub/GitLab root. SDK UUID comes from that cloud detail JSON.
XML icons and the sole v5 baseline remain in the AAR; v4 and version.prop are absent.

Exact lookup includes literal regex strings. v5 regex matching uses
whole-string matches in `(priority, _id)` order. Android ICU digit escapes are
normalized to ASCII, preserving escaped backslashes and character classes; v5
regex evaluation rejects the five line separators defined by the producer. Closing a reader and querying it
are synchronized. The singleton swaps only after a successful open and serializes
queries with initialization/close/activation; hosts must synchronize their own
download directory/pointer updates. Prune the download Store before activation,
and never prune Bundle's separate baseline directory.

`Rule` is a seven-field Parcelable data class with generated copy, destructuring,
equality and hash operations. Parcel bytes are not a persistent storage format.
Use `getMetadata().dataVersion` for the Long data version. Metadata access requires
an open v5 reader; it does not invent a fallback version before initialization or
after close.

## Unpublished local integration and checks

Build `:library:assembleRelease` in this checkout. The actual unpublished output
is `library/build/outputs/aar/library-release.aar`. The LibChecker app accepts:

```sh
./gradlew :app:assembleFossDebug \
  -PrulesBundleAar=/absolute/path/to/LibChecker-Rules-Bundle/library/build/outputs/aar/library-release.aar
```

Its dependency selection uses `implementation(files(path))` instead of the normal
Maven dependency when that property is supplied. The library adds no runtime
dependencies beyond Kotlin already used by the app. Rebuild this AAR after Bundle
source changes. No publication or invented Maven version is required.

A composite `includeBuild` substitution can work when the host uses a compatible
Gradle/AGP toolchain. It does **not** currently work with LibChecker's Gradle 9.7 /
AGP 9.4: Bundle's AGP 8.11 references Gradle internals removed in Gradle 9.6.
Use the separately built AAR for this host. Keep this repository's Gradle wrapper
for its standalone checks. Library code releases and Rules data releases are
independent.

Build the library and test APK (JDK 17 for this standalone Gradle wrapper):

```sh
./gradlew :library:assembleRelease :library:assembleDebugAndroidTest \
  -PrulesTestBundleDir=/absolute/path/to/unpacked/android-v5
```

The optional directory must be actual DB-only producer output. Tests verify
Context initialization/repair, update activation, all v5 record names, v4 rejection
without replacing current v5, cloud URL selection, drawable fallback and absence
of local details/icons. Without it, the producer integration test is explicitly
skipped. Test data is only included in the test APK.

Use a coordinated device window and install the test APK with `adb -s <serial>
install -r`; run selected checks with `adb -s <serial> shell am instrument -w -e
class <class#method> com.absinthe.lc.rulesbundle.test/androidx.test.runner.AndroidJUnitRunner`.
Do not use Gradle connected tasks on the shared device: UTP cleanup can uninstall
an app under test. Never clear or uninstall the user's app as part of validation.

## Sync Android drawable resources

From a committed Rules source revision:

```sh
python3 tools/sync_android_icons.py --rules-dir /path/to/LibChecker-Rules \
  --revision <full-40-character-commit-sha>
python3 tools/sync_android_icons.py --rules-dir /path/to/LibChecker-Rules \
  --revision <full-40-character-commit-sha> --check
python3 -B -m unittest discover -s tools -p 'test_*.py'
```

The script reads pinned `icons/index.json` and `icons/android/<iconId>.xml`
through Git, validates/copies actual VectorDrawables and generates `IconResMap`.
It rejects removal or reassignment of old indexes and missing previously shipped
vectors. New web-only indexes map to the placeholder until an Android XML exists.
The generated map records its source commit; Gradle compiles the copied XML.
No SVG-to-Android conversion or network fetch is involved.

Rules reusing shipped icons need only a data release. A new Android icon needs
this resource sync, a Bundle code/resource release and an App dependency update.
A data download cannot add a drawable to an already installed App. The original
XML-to-SVG producer converter continues to serve portable/web consumers.

## Update the packaged baseline

The small updater and automatic pull-request workflow now live in this repository.
Normal builds consume checked-in data; they do not fetch a floating release.

```sh
python3 tools/update-rules-bundle.py --manifest <producer-manifest> --root <producer-output-root>
python3 tools/update-rules-bundle.py --manifest <producer-manifest> --root <producer-output-root> --check
./gradlew :library:assembleRelease :library:assembleDebugAndroidTest
```

The updater verifies the Android ZIP SHA256, size, exact two-entry layout, extraction
limits and metadata identity. It writes only raw DB + metadata to AAR assets and
keeps the full producer lock at `rules/manifest.json`, outside packaged assets.
It never also stores the ZIP. An existing same-version lock can only be reverified,
not changed; changed data requires a forward version. The scheduled/manual
`.github/workflows/update-rules-bundle.yml` uses the same updater, builds the Bundle
and opens an own-repository PR. It does not publish a release or modify the App.
New Android icons still use the separate pinned resource sync and
require a resource release; a baseline data update does not create drawables.
