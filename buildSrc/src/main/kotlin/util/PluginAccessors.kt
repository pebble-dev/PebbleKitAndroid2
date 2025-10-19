import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

inline val PluginDependenciesSpec.commonAndroid: PluginDependencySpec
    get() = id("android-module-commons")

inline val PluginDependenciesSpec.androidLibraryModule: PluginDependencySpec
    get() = id("library-android-module")

inline val PluginDependenciesSpec.pureKotlinModule: PluginDependencySpec
    get() = id("pure-kotlin-module")

inline val PluginDependenciesSpec.parcelize: PluginDependencySpec
    get() = id("kotlin-parcelize")
