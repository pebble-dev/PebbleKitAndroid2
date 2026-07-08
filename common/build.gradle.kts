plugins {
    androidLibraryModule
    publishToMaven
}

android {
    buildFeatures {
        aidl = true
    }
}

dependencies {
    api(projects.commonApi)
    api(libs.kotlin.coroutines)
    implementation(libs.kermit)
}
