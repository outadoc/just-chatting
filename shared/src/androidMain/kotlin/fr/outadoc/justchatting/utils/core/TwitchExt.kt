package fr.outadoc.justchatting.utils.core

import com.eygraber.uri.Uri

fun String.createChannelExternalLink(): Uri =
    Uri.parse("https://twitch.tv")
        .buildUpon()
        .appendPath(this)
        .build()
