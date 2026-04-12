package fr.outadoc.justchatting.feature.chat.presentation

import coil3.PlatformContext
import fr.outadoc.justchatting.feature.shared.domain.model.User

public interface ChatNotifier {
    public val areNotificationsEnabled: Boolean

    public fun notify(
        context: PlatformContext,
        user: User,
    )
}
