plugins {
    androidLibraryModule
    publishToMaven
}

android {
    namespace = "io.rebble.pebblekit.server"

    androidResources.enable = true
}

dependencies {
    api(projects.common)
    api(projects.commonApi)
    api(projects.serverApi)
    api(libs.androidx.core)
    api(libs.kotlin.coroutines)
    api(libs.kermit)

    implementation(projects.common)
}

dependencyAnalysis {
    issues {
        onIncorrectConfiguration {
            // We want consumers of this library to access the common api endpoints, even if this library is not
            // using them directly
            exclude(projects.common)
        }
    }
}
