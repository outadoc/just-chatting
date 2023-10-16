package fr.outadoc.justchatting.feature.chat.presentation.mobile

import fr.outadoc.justchatting.component.deeplink.DeeplinkDefinitions

fun createChannelDeeplink(channelLogin: String): String =
    DeeplinkDefinitions.ViewChannel
        .buildUpon()
        .appendPath(channelLogin)
        .build()
        .toString()
