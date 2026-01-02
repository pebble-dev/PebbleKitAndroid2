plugins {
    androidLibraryModule
    publishToMaven
}

android {
    namespace = "io.rebble.pebblekit.client.java"

    androidResources.enable = true
}

dependencies {
    api(projects.commonApi)
    api(projects.client)
    api(projects.clientApi)

    implementation(libs.androidx.datastore.preferences)
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
