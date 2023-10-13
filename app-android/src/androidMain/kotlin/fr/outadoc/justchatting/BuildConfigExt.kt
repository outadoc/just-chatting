package fr.outadoc.justchatting

enum class BuildType {
    Debug, QA, Release
}

@Suppress("KotlinConstantConditions")
val buildType: BuildType
    get() = when (val type = BuildConfig.BUILD_TYPE) {
        "debug" -> BuildType.Debug
        "qa" -> BuildType.QA
        "release" -> BuildType.Release
        else -> error("unregistered build type: $type")
    }
