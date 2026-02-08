import gradle.kotlin.dsl.accessors._df438b9d261df97d39394f67dfadf160.mavenPublishing


plugins {
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

group = "io.rebble.pebblekit2"
version = File(rootDir, "version.txt").readText().trim()

mavenPublishing {
    publishToMavenCentral(automaticRelease = true)

    if (System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey") != null) {
        signAllPublications()
    }

    pom {
        val projectGitUrl = "https://github.com/pebble-dev/PebbleKitAndroid2"
        name.set(project.name)
        description.set(
            "A new modern PebbleKit API for communication between phone companion apps and " +
                "the watchapps on the Pebble-OS running watches."
        )
        url.set(projectGitUrl)
        inceptionYear.set("2025")
        licenses {
            license {
                name.set("Apache License 2.0")
                url.set("$projectGitUrl/blob/main/LICENSE")
            }
        }
        issueManagement {
            system.set("GitHub")
            url.set("$projectGitUrl/issues")
        }
        scm {
            connection.set("scm:git:$projectGitUrl")
            developerConnection.set("scm:git:$projectGitUrl")
            url.set(projectGitUrl)
        }
        developers {
            developer {
                name.set("Rebble")
                url.set("https://rebble.io/")
            }
        }
    }
}
