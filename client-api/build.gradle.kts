plugins {
    pureKotlinModule
    publishToMaven
}

dependencies {
    api(projects.commonApi)
    implementation(libs.androidx.annotation)
    implementation(libs.kotlin.coroutines)
}
