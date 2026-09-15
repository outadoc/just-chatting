package fr.outadoc.justchatting.feature.chat.presentation.ui

import android.content.Context
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.shared.domain.model.User

internal class AndroidCreateShortcutForChannelUseCase(
    private val context: Context,
) : CreateShortcutForChannelUseCase {
    override operator fun invoke(user: User) {
        publishConversationShortcut(context = context, user = user)
    }
}
