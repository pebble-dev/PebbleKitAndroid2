import com.android.build.api.dsl.LibraryAndroidResources
import com.android.build.gradle.tasks.asJavaVersion
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import util.commonAndroid

val libs = the<LibrariesForLibs>()

plugins {
    id("org.jetbrains.kotlin.android")

    id("all-modules-commons")
    id("org.gradle.android.cache-fix")
}

commonAndroid {
    // Use default namespace for no resources, modules that use resources must override this
    // Add a unique suffix to every module to stop AGP from complaining about "is used in multiple modules"
    // Workaround for the https://issuetracker.google.com/issues/332947919
    val uniqueNamespaceSuffix = path.removePrefix(":").replace(':', '.').replace("-", "")
    namespace = "io.rebble.pebblekit2.noresources.$uniqueNamespaceSuffix"

    compileSdk = 36

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        minSdk = 24
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildFeatures {
        buildConfig = false
        resValues = false
        shaders = false
    }

    val androidResources = androidResources
    if (androidResources is LibraryAndroidResources) {
        androidResources.enable = false
    }
    compileOptions {
        // Android still creates java tasks, even with 100% Kotlin.
        // Ensure that target compatiblity is equal to kotlin's jvmToolchain
        lateinit var javaVersion: JavaVersion
        the<KotlinProjectExtension>().jvmToolchain { javaVersion = this.languageVersion.get().asJavaVersion() }

        targetCompatibility = javaVersion
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
        freeCompilerArgs.add("-opt-in=kotlinx.coroutines.FlowPreview")
    }
}
