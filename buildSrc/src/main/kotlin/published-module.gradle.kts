plugins {
    id("com.vanniktech.maven.publish")
}

// No javadoc for now
// https://github.com/Kotlin/dokka/issues/2956
tasks
    .matching { task ->
        task.name.contains("javaDocReleaseGeneration") ||
            task.name.contains("javaDocDebugGeneration")
    }.configureEach {
        enabled = false
    }
