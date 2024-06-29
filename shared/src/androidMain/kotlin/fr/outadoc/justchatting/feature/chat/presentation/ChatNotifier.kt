package fr.outadoc.justchatting.feature.chat.presentation

import android.content.Context
import fr.outadoc.justchatting.feature.home.domain.model.User

internal interface ChatNotifier {
    val areNotificationsEnabled: Boolean
    fun notify(context: Context, user: User)
    fun dismissNotification(context: Context, channelId: String)
}
