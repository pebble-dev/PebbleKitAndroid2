import nl.littlerobots.vcu.plugin.resolver.ModuleVersionCandidate
import nl.littlerobots.vcu.plugin.versionSelector

// Please do not add any subprojects {} / allprojects {} blocks or anything else that affects suborpojects to allow for
// project isolation when it comes out (https://gradle.github.io/configuration-cache/#project_isolation)

plugins {
    id("com.autonomousapps.dependency-analysis")
    alias(libs.plugins.versionCatalogUpdate)
    id("kotlinova")
}

dependencyAnalysis {
    structure {
        ignoreKtx(true)

        // Library Groups:

        bundle("datastore") {
            includeGroup("androidx.datastore")
        }

        bundle("kermit") {
            includeGroup("co.touchlab")
        }

        bundle("compose") {
            // Compose libraries are blanket-included to for convenience. It shouldn't cause a big issue
            includeGroup("androidx.compose.animation")
            includeGroup("androidx.compose.foundation")
            includeGroup("androidx.compose.material")
            includeGroup("androidx.compose.material3")
            includeGroup("androidx.compose.runtime")
            includeGroup("androidx.compose.ui")
        }
    }
}

versionCatalogUpdate {
    catalogFile.set(file("config/libs.toml"))

    fun ModuleVersionCandidate.newlyContains(keyword: String): Boolean {
        return !currentVersion.contains(keyword, ignoreCase = true) && candidate.version.contains(
            keyword,
            ignoreCase = true
        )
    }

    versionSelector {
        !it.newlyContains("alpha") &&
                !it.newlyContains("beta") &&
                !it.newlyContains("RC") &&
                !it.newlyContains("M") &&
                !it.newlyContains("eap") &&
                !it.newlyContains("dev") &&
                !it.newlyContains("pre")
    }
}

// Always update to the ALL distribution when updating Gradle
tasks.wrapper {
    distributionType = Wrapper.DistributionType.ALL
}
