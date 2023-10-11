package fr.outadoc.justchatting.feature.chat.presentation

import android.content.Context
import fr.outadoc.justchatting.component.chatapi.domain.model.User

interface ChatNotifier {
    val areNotificationsEnabled: Boolean
    fun notify(context: Context, user: User)
    fun dismissNotification(context: Context, channelId: String)
}