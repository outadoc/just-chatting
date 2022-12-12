@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "just-chatting"

include(":app")

include(":utils-core")
include(":utils-ui")
include(":utils-logging")

include(":feature-preferences-data")
include(":feature-chat-data")
include(":feature-chat-domain")
include(":feature-chat-presentation")

include(":component-twitch-data")
include(":component-twitch-domain")
include(":component-preferences-domain")

