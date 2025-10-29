plugins {
    androidLibraryModule
}

android {
    namespace = "io.rebble.pebblekit.client"

    androidResources.enable = true
}

dependencies {
    api(projects.common)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core)
    implementation(libs.kermit)
    implementation(libs.kotlin.coroutines)
}
