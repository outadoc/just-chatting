package fr.outadoc.justchatting.feature.chat.presentation.mobile

import android.content.Context
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier

class NoOpChatNotifier : ChatNotifier {

    override val areNotificationsEnabled: Boolean = false

    override fun notify(context: Context, user: User) {}

    override fun dismissNotification(context: Context, channelId: String) {}
}
