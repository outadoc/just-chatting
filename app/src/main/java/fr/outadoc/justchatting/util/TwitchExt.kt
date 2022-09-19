package fr.outadoc.justchatting.util

import android.net.Uri

fun String.createChannelExternalLink(): Uri =
    Uri.parse("https://twitch.tv")
        .buildUpon()
        .appendPath(this)
        .build()
