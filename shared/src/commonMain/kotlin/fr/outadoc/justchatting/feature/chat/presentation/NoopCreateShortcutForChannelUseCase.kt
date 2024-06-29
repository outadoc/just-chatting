package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.home.domain.model.User

internal class NoopCreateShortcutForChannelUseCase : CreateShortcutForChannelUseCase {
    override fun invoke(user: User) {}
}
