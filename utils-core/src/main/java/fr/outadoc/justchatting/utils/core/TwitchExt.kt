package fr.outadoc.justchatting.utils.core

import android.net.Uri

fun String.createChannelExternalLink(): Uri =
    Uri.parse("https://twitch.tv")
        .buildUpon()
        .appendPath(this)
        .build()
