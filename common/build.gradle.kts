plugins {
    androidLibraryModule
}

android {
    buildFeatures {
        aidl = true
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kermit)
}
