package fr.outadoc.justchatting.feature.chat.presentation

import coil3.PlatformContext
import fr.outadoc.justchatting.feature.shared.domain.model.User

internal interface ChatNotifier {
    val areNotificationsEnabled: Boolean
    fun notify(context: PlatformContext, user: User)
    fun dismissNotification(context: PlatformContext, channelId: String)
}
