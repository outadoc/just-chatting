package fr.outadoc.justchatting.util

import android.net.Uri

fun formatChannelUri(channelLogin: String): Uri =
    Uri.parse("https://twitch.tv")
        .buildUpon()
        .appendPath(channelLogin)
        .build()
