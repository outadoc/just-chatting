package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.component.chatapi.domain.model.User

class NoopCreateShortcutForChannelUseCase : CreateShortcutForChannelUseCase {
    override fun invoke(user: User) {}
}
