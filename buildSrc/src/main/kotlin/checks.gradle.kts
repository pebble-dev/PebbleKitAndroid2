import dev.detekt.gradle.Detekt
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.accessors.dm.LibrariesForLibs
import si.inova.kotlinova.gradle.KotlinovaExtension
import util.commonAndroid
import util.isAndroidProject

val libs = the<LibrariesForLibs>()

plugins {
    id("kotlinova")
}

// Apply detekt the old way until https://github.com/detekt/detekt/issues/8977 is solved
apply(plugin = "dev.detekt")

if (isAndroidProject()) {
    commonAndroid {
        lint {
            lintConfig = file("$rootDir/config/android-lint.xml")
            abortOnError = true

            warningsAsErrors = true
            sarifReport = true
        }
    }
}

configure<DetektExtension> {
    config.setFrom("$rootDir/config/detekt.yml")
}

tasks.withType<Detekt>().configureEach {
    val buildDir = project.layout.buildDirectory.asFile.get().absolutePath
    // Exclude all generated files
    exclude {
        it.file.absolutePath.contains(buildDir)
    }
}

configure<KotlinovaExtension> {
    mergeDetektSarif = true
    if (isAndroidProject()) {
        mergeAndroidLintSarif = true
    }

    enableDetektPreCommitHook = true
}

dependencies {
    add("detektPlugins", libs.detekt.ktlint)
}
