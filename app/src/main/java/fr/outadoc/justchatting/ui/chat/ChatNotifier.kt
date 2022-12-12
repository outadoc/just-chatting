package fr.outadoc.justchatting.ui.chat

import android.content.Context
import fr.outadoc.justchatting.component.twitch.model.User

interface ChatNotifier {
    fun notify(context: Context, user: User)
    fun dismissNotification(context: Context, channelId: String)
}
