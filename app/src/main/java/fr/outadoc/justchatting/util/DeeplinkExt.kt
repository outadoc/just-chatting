package fr.outadoc.justchatting.util

import android.net.Uri
import fr.outadoc.justchatting.deeplink.DeeplinkDefinitions

fun String.createChannelDeeplink(): Uri =
    DeeplinkDefinitions.ViewChannel
        .buildUpon()
        .appendPath(this)
        .build()
