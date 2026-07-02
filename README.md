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

Initialize SDK in `Application` class
```kotlin
class App : Application() {

  override fun onCreate() {
    super.onCreate()
    LCRules.init(this)
    
    // Optional: set online repo (GitHub repo as default)
    LCRules.setRemoteRepo(LCRemoteRepo.GitHub)

    // Optional: reserved for future rule locales
    LCRules.setLocale(LCLocale.ZH)
  }
}
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

The SDK reads the bundled rules database through Android's SQLite APIs. It does
not require Room, AppCompat, Core KTX, or kotlinx-coroutines in host apps.
Choose the coroutine context for `getRule(...)` in your app.

## Migration

- Remove Room, AppCompat, and Core KTX dependencies if they were added only for
  this package.
- Keep calling `LCRules.init(this)` from `Application.onCreate()`.
- Use `LCRemoteRepo.GitHub` / `LCRemoteRepo.GitLab`. The old `Github` /
  `Gitlab` aliases still work, but are deprecated.
- `LCRules.setLocale(...)` is kept for future rule locales. It is currently a
  no-op because bundled rules contain one label locale.
- Use `LCRules.close()` instead of `closeDb()`.
- Do not use the old internal database classes (`RuleDao`, `RuleDatabase`,
  `RuleRepository`, `Repositories`, `RuleEntity`, or `IAPI`). Query rules only
  through `LCRules.getRule(...)`.

## JitPack

JitPack builds with JDK 17 and publishes the library module through
`:library:publishToMavenLocal`.
