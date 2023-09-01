pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
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

include(":component-preferences-data")

include(":feature-chat-data")
include(":feature-chat-domain")
include(":feature-chat-presentation")
include(":feature-chat-presentation-mobile")

include(":feature-home-presentation")
include(":feature-home-presentation-mobile")

include(":feature-preferences-presentation")
include(":feature-preferences-presentation-mobile")

include(":feature-pronouns-data")
include(":feature-pronouns-domain")

include(":component-deeplink")

include(":component-preferences-domain")

include(":component-chatapi-common")
include(":component-chatapi-db")
include(":component-chatapi-twitch")
include(":component-chatapi-domain")
