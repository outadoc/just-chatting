package fr.outadoc.justchatting.feature.chat.presentation.ui

import android.content.Context
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.chat.presentation.ProfileImageCache
import fr.outadoc.justchatting.feature.shared.domain.model.User

internal class AndroidCreateShortcutForChannelUseCase(
    private val context: Context,
    private val profileImageCache: ProfileImageCache,
) : CreateShortcutForChannelUseCase {
    override operator fun invoke(user: User) {
        // The shortcut icon is a content URI into UserProfileImageContentProvider, which only
        // serves what is already on disk, so make sure it gets there.
        profileImageCache.prefetch(user.id)

        publishConversationShortcut(context = context, user = user)
    }
}
