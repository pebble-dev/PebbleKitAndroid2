plugins {
    androidLibraryModule
}

android {
    buildFeatures {
        aidl = true
    }
}

dependencies {
    api(projects.commonApi)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kermit)
}
