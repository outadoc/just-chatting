package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.shared.domain.model.User

internal interface CreateShortcutForChannelUseCase {
    operator fun invoke(user: User)
}
