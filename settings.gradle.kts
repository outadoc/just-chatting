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

include(":component-preferences")
include(":component-chat-data")
include(":component-chat-domain")
include(":component-twitch-data")
include(":component-twitch-domain")
