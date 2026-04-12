package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.shared.domain.model.User

internal class NoopChatNotifier : ChatNotifier {
    override val areNotificationsEnabled: Boolean = false

    override fun notify(user: User) {
    }
}
