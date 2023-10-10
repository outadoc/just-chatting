package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.component.chatapi.domain.model.User

interface CreateShortcutForChannelUseCase {
    operator fun invoke(user: User)
}