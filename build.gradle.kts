import org.jetbrains.compose.internal.utils.getLocalProperty
import java.net.URI

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kmp) apply false
    alias(libs.plugins.multiplatform.swiftpackage) apply false
    alias(libs.plugins.dokka)
    id("maven-publish")
}

subprojects {
    group = "io.github.kalinjul.kotlin.multiplatform"
}

subprojects {
    apply(plugin = "maven-publish")
    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/egeniq-forks/kotlin-multiplatform-oidc")
                credentials {
                    username = getLocalProperty("GITHUB_USER") ?: System.getenv("GITHUB_USER")
                    password = getLocalProperty("GITHUB_OIDC_TOKEN") ?: System.getenv("GITHUB_OIDC_TOKEN")
                }
            }
        }
    }
}
