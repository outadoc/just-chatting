package fr.outadoc.justchatting.feature.chat.presentation.mobile

import fr.outadoc.justchatting.feature.deeplink.DeeplinkDefinitions

internal fun createChannelDeeplink(channelLogin: String): String =
    DeeplinkDefinitions.ViewChannel
        .buildUpon()
        .appendPath(channelLogin)
        .build()
        .toString()
