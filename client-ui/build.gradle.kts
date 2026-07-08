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

    api(libs.androidx.compose.material3)
    api(libs.kotlin.coroutines)
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
