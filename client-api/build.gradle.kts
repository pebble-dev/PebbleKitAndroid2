plugins {
    pureKotlinModule
    publishToMaven
}

dependencies {
    api(projects.commonApi)
    api(libs.kotlin.coroutines)
    implementation(libs.androidx.annotation)
}
