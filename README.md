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

    // Required: Kotlin Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.0'
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
val rule: Rule = LCRules.getRule(name = "libflutter.so", type = NATIVE, useRegex = false)

// Activity library
val rule2: Rule = LCRules.getRule(name = "androidx.compose.ui.tooling.PreviewActivity", type = ACTIVITY, useRegex = false)

// Query library with RegEx
val rule3: Rule = LCRules.getRule(name = "libAMapSDK_MAP_v7_9_1.so", type = NATIVE, useRegex = true)
```
