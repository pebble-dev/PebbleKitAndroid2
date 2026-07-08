package util

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Action
import org.gradle.api.Project

/**
 * android {} block that can be used without applying specific android plugin
 */
fun Project.commonAndroid(
    block: Action<CommonExtension>,
) {
    (this as org.gradle.api.plugins.ExtensionAware).extensions.configure("android", block)
}

fun Project.isAndroidProject(): Boolean {
    return pluginManager.hasPlugin("com.android.base")
}
