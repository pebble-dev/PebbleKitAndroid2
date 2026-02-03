plugins {
    androidLibraryModule
    publishToMaven
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "io.rebble.pebblekit.client.ui"

    androidResources.enable = true
}

dependencies {
    api(projects.clientApi)

    implementation(libs.accompanist.drawablepainter)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.kotlin.coroutines)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
