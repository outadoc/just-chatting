package com.github.andreyasadchy.xtra.util

object TwitchApiHelper {
    var checkedValidation = false
}

fun getTemplateUrl(url: String?, type: String): String? {
    if (url.isNullOrEmpty() || url.startsWith("https://vod-secure.twitch.tv/_404/404_processing"))
        return when (type) {
            "game" -> "https://static-cdn.jtvnw.net/ttv-static/404_boxart.jpg"
            "video" -> "https://vod-secure.twitch.tv/_404/404_processing_320x180.png"
            else -> null
        }

    val width = when (type) {
        "game" -> "285"
        "video" -> "1280"
        "profileimage" -> "300"
        else -> ""
    }

    val height = when (type) {
        "game" -> "380"
        "video" -> "720"
        "profileimage" -> "300"
        else -> ""
    }

    val reg1 = """-\d\d\dx\d\d\d""".toRegex()
    val reg2 = """\d\d\d\dx\d\d\d""".toRegex()
    val reg3 = """\d\d\dx\d\d\d""".toRegex()
    val reg4 = """\d\dx\d\d\d""".toRegex()
    val reg5 = """\d\d\dx\d\d""".toRegex()
    val reg6 = """\d\dx\d\d""".toRegex()

    if (type == "clip") {
        return if (reg1.containsMatchIn(url)) {
            reg1.replace(url, "")
        } else {
            url
        }
    }

    return when {
        url.contains("%{width}", true) -> url.replace("%{width}", width)
            .replace("%{height}", height)
        url.contains("{width}", true) -> url.replace("{width}", width)
            .replace("{height}", height)
        reg2.containsMatchIn(url) -> reg2.replace(url, "${width}x$height")
        reg3.containsMatchIn(url) -> reg3.replace(url, "${width}x$height")
        reg4.containsMatchIn(url) -> reg4.replace(url, "${width}x$height")
        reg5.containsMatchIn(url) -> reg5.replace(url, "${width}x$height")
        reg6.containsMatchIn(url) -> reg6.replace(url, "${width}x$height")
        else -> url
    }
}
