plugins {
    androidLibraryModule
}

android {
    buildFeatures {
        aidl = true
    }
}

dependencies {
    implementation(libs.kotlin.coroutines)
    implementation(libs.kermit)
}
