plugins {
    androidLibraryModule
}

dependencies {
    api(projects.common)
    api(libs.androidx.core)
    api(libs.kotlin.coroutines)
    api(libs.kermit)
}
