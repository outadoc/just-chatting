package fr.outadoc.justchatting.feature.chat.presentation.mobile

import android.net.Uri
import fr.outadoc.justchatting.component.deeplink.DeeplinkDefinitions

fun String.createChannelDeeplink(): Uri =
    DeeplinkDefinitions.ViewChannel
        .buildUpon()
        .appendPath(this)
        .build()
