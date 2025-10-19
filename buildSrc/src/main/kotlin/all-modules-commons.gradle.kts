import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

val libs = the<LibrariesForLibs>()

plugins {
    id("checks")
    id("dependency-analysis")
}

configure<KotlinProjectExtension> {
    jvmToolchain(21)

    explicitApi()
}
