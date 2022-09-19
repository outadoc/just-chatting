package fr.outadoc.justchatting.util

import android.net.Uri

fun String.createChannelDeeplink(): Uri =
    Uri.parse("justchatting://channel")
        .buildUpon()
        .appendPath(this)
        .build()

fun Uri.parseChannelLogin(): String? {
    if (scheme != "justchatting") return null
    if (host != "channel") return null
    return pathSegments.firstOrNull()
}
