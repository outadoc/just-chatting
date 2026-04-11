package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.shared.domain.model.User

public class NoopCreateShortcutForChannelUseCase : CreateShortcutForChannelUseCase {
    override fun invoke(user: User) {}
}
