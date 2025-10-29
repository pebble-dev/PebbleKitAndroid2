plugins {
    androidLibraryModule
}

android {
    namespace = "io.rebble.pebblekit.server"

    androidResources.enable = true
}

dependencies {
    api(projects.common)
    api(libs.androidx.core)
    api(libs.kotlin.coroutines)
    api(libs.kermit)
}
