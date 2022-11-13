package fr.outadoc.justchatting.util

import android.net.Uri

val viewChannelBaseUrl: Uri = Uri.parse("justchatting://channel")

fun String.createChannelDeeplink(): Uri =
    viewChannelBaseUrl
        .buildUpon()
        .appendPath(this)
        .build()
