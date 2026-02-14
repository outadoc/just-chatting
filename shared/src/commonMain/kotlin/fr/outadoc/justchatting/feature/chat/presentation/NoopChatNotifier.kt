package fr.outadoc.justchatting.feature.chat.presentation

import coil3.PlatformContext
import fr.outadoc.justchatting.feature.shared.domain.model.User

internal class NoopChatNotifier : ChatNotifier {
    override val areNotificationsEnabled: Boolean = false

    override fun notify(
        context: PlatformContext,
        user: User,
    ) {
    }
}
