pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Include PebbleKit library until
// it is stable enough to make a release
includeBuild("../..") {
    dependencySubstitution {
        substitute(module("io.rebble:pebblekit2-java"))
            .using(project(":client-java"))
    }
}

rootProject.name = "PebbleKit2_JavaSample"
include(":app")
