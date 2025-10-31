plugins {
    androidLibraryModule
}

android {
    namespace = "io.rebble.pebblekit.server"

    androidResources.enable = true
}

dependencies {
    api(projects.common)
}
