package fr.outadoc.justchatting.feature.chat.presentation.ui

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.deeplink.DeeplinkDefinitions

internal fun createChannelDeeplink(userId: String): Uri = DeeplinkDefinitions.ViewChannel
    .buildUpon()
    .appendPath(userId)
    .build()
