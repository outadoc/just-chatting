package fr.outadoc.justchatting.utils.core

import com.eygraber.uri.Uri

internal fun createChannelExternalLink(channelLogin: String): String =
    Uri.parse("https://twitch.tv")
        .buildUpon()
        .appendPath(channelLogin)
        .build()
        .toString()
