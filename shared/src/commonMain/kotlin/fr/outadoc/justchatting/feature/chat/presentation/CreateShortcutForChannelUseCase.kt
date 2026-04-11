package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.shared.domain.model.User

public interface CreateShortcutForChannelUseCase {
    public operator fun invoke(user: User)
}
