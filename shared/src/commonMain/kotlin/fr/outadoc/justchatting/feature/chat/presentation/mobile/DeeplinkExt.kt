package fr.outadoc.justchatting.feature.chat.presentation.mobile

import fr.outadoc.justchatting.feature.deeplink.DeeplinkDefinitions

internal fun createChannelDeeplink(userId: String): String =
    DeeplinkDefinitions.ViewChannel
        .buildUpon()
        .appendPath(userId)
        .build()
        .toString()
