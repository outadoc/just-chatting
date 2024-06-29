package fr.outadoc.justchatting.feature.chat.presentation.mobile

import android.content.Context
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import fr.outadoc.justchatting.feature.chat.presentation.CreateShortcutForChannelUseCase
import fr.outadoc.justchatting.feature.chat.presentation.getProfileImageIcon
import fr.outadoc.justchatting.feature.home.domain.model.User

internal class MobileCreateShortcutForChannelUseCase(
    private val context: Context,
) : CreateShortcutForChannelUseCase {

    override operator fun invoke(user: User) {
        val intent = ChatActivity.createIntent(context, user.login)
        val person: Person =
            Person.Builder()
                .setKey(user.id)
                .setName(user.displayName)
                .setIcon(user.getProfileImageIcon(context))
                .build()

        val maxShortcutCount = ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)
        val currentShortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)
        val alreadyPublished = currentShortcuts.any { it.id == user.id }

        if (currentShortcuts.size >= maxShortcutCount && !alreadyPublished) {
            val oldest = currentShortcuts
                .filterNot { it.id == user.id }
                .minByOrNull { shortcut -> shortcut.lastChangedTimestamp }

            oldest?.let { shortcut ->
                ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(shortcut.id))
            }
        }

        ShortcutManagerCompat.addDynamicShortcuts(
            context,
            listOf(
                ShortcutInfoCompat.Builder(context, user.id)
                    .setIntent(intent)
                    .setLongLived(true)
                    .setIcon(user.getProfileImageIcon(context))
                    .setShortLabel(user.displayName)
                    .setPerson(person)
                    .setIsConversation()
                    .build(),
            ),
        )
    }
}
