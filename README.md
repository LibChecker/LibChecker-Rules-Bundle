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
    
    // WIP: set rules locale
    LCRules.setLocale(LCLocale.ZH)
  }
```

Get marked rule
```kotlin
// Native library
val rule: Rule = LCRules.getRule(libName = "libflutter.so", type = NATIVE, useRegex = false)

// Activity library
val rule2: Rule = LCRules.getRule(libName = "androidx.compose.ui.tooling.PreviewActivity", type = ACTIVITY, useRegex = false)

// Query library with RegEx
val rule3: Rule = LCRules.getRule(libName = "libAMapSDK_MAP_v7_9_1.so", type = NATIVE, useRegex = true)
```

The SDK reads the bundled rules database through Android's SQLite APIs. It does
not require Room or AppCompat in host apps.

## Migration

- Remove Room, AppCompat, and Core KTX dependencies if they were added only for
  this package.
- Keep calling `LCRules.init(this)` from `Application.onCreate()`.
- Use `LCRemoteRepo.GitHub` / `LCRemoteRepo.GitLab`. The old `Github` /
  `Gitlab` aliases still work, but are deprecated.
- Use `LCRules.close()` instead of `closeDb()`.
- Do not use the old internal database classes (`RuleDao`, `RuleDatabase`,
  `RuleRepository`, `Repositories`, `RuleEntity`, or `IAPI`). Query rules only
  through `LCRules.getRule(...)`.

## JitPack

JitPack builds with JDK 17 and publishes the library module through
`:library:publishToMavenLocal`.
