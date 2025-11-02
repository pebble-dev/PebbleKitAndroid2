plugins {
    androidLibraryModule
}

android {
    namespace = "io.rebble.pebblekit.client"

    androidResources.enable = true
}

dependencies {
    api(projects.common)
    api(projects.commonApi)
    api(projects.clientApi)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core)
    implementation(libs.kermit)
    implementation(libs.kotlin.coroutines)
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
