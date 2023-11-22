## iOS App usage

Info.plist / Project -> Target -> Info -> URL Types:
Add redirect schema (ex. org.publicvalue.multiplatform.oidc.sample)

## Android App usage

build.gradle.kts:
```
    defaultConfig {
        addManifestPlaceholders(
            mapOf("oidcRedirectScheme" to "<uri scheme>")
        )
    }
```

If custom tab is not closing:
```kotlin
HandleRedirectActivity.mainActivityClass = this.javaClass.kotlin
```