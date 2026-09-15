package fr.outadoc.justchatting.feature.chat.presentation.ui

import android.content.Context
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import fr.outadoc.justchatting.feature.chat.presentation.getProfileImageIcon
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.shared.presentation.ui.MainActivity
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.logging.logWarning

private const val TAG = "ConversationShortcuts"

/**
 * Flags that match the shortcut kinds the notification service itself considers valid when it
 * resolves a conversation notification's shortcut id (see AOSP `ShortcutHelper.getValidShortcutInfo`).
 */
private const val MATCH_FLAGS =
    ShortcutManagerCompat.FLAG_MATCH_DYNAMIC or
        ShortcutManagerCompat.FLAG_MATCH_PINNED or
        ShortcutManagerCompat.FLAG_MATCH_CACHED

/**
 * Publishes the long-lived conversation shortcut a bubble notification for [user] depends on.
 *
 * The system resolves the notification's shortcut id synchronously while the notification is being
 * enqueued; if no valid shortcut is found at that exact moment, the bubble metadata is dropped and
 * the notification posts as an ordinary one. So this has to have succeeded before we notify.
 *
 * @return whether the shortcut was published.
 */
internal fun publishConversationShortcut(
    context: Context,
    user: User,
): Boolean {
    val person: Person =
        Person
            .Builder()
            .setKey(user.id)
            .setName(user.displayName)
            .setIcon(user.getProfileImageIcon(context))
            .build()

    val shortcut =
        ShortcutInfoCompat
            .Builder(context, user.id)
            .setIntent(MainActivity.createIntent(context = context, userId = user.id))
            .setLongLived(true)
            .setIcon(user.getProfileImageIcon(context))
            .setShortLabel(user.displayName)
            .setPerson(person)
            .setIsConversation()
            .build()

    return try {
        // pushDynamicShortcut, unlike addDynamicShortcuts, is meant to be called repeatedly for
        // conversations: it isn't rate-limited, and it evicts the lowest-ranked shortcut itself when
        // the per-activity limit is reached.
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut).also { published ->
            if (!published) {
                logWarning(TAG) {
                    "Conversation shortcut for ${user.displayName} (${user.id}) was not fully " +
                        "published; bubbles may not work for this channel"
                }
            }
        }
    } catch (e: IllegalArgumentException) {
        logError(TAG, e) { "Rejected conversation shortcut for ${user.displayName} (${user.id})" }
        false
    } catch (e: IllegalStateException) {
        logError(TAG, e) { "Could not publish conversation shortcut for ${user.displayName} (${user.id})" }
        false
    }
}

/**
 * Whether a shortcut the notification service would accept as a conversation shortcut currently
 * exists for [userId].
 *
 * AOSP `ShortcutHelper.isConversationShortcut` also requires the shortcut to be long-lived, but
 * there is no public accessor for that; [publishConversationShortcut] is the only place we create
 * these and it always marks them long-lived, so presence and enablement are what's left to check.
 */
internal fun hasValidConversationShortcut(
    context: Context,
    userId: String,
): Boolean =
    try {
        ShortcutManagerCompat
            .getShortcuts(context, MATCH_FLAGS)
            .any { shortcut -> shortcut.id == userId && shortcut.isEnabled }
    } catch (e: IllegalStateException) {
        // Shortcuts can't be read while the user is locked.
        logWarning(TAG) { "Could not read conversation shortcuts: ${e.message}" }
        false
    }
