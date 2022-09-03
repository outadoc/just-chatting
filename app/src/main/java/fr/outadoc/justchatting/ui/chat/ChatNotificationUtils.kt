package fr.outadoc.justchatting.ui.chat

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.LocusIdCompat
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.util.toPendingIntent

object ChatNotificationUtils {

    private const val NOTIFICATION_CHANNEL_ID = "channel_bubble"

    fun areBubblesAllowed(
        context: Context,
        channel: NotificationChannelCompat
    ): Boolean {
        if (channel.canBubble()) return true

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false

        val nm = context.getSystemService<NotificationManager>() ?: return false

        if (!nm.areNotificationsEnabled()) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            nm.bubblePreference != NotificationManager.BUBBLE_PREFERENCE_NONE
        ) return true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && nm.areBubblesAllowed())
            return true

        return false
    }

    private fun notificationIdFor(channelId: String) = channelId.hashCode()

    fun createShortcutForChannel(
        context: Context,
        intent: Intent,
        channelId: String,
        channelName: String,
        person: Person,
        icon: IconCompat
    ) {
        val maxShortcutCount = ShortcutManagerCompat.getMaxShortcutCountPerActivity(context)
        val currentShortcuts = ShortcutManagerCompat.getDynamicShortcuts(context)
        val alreadyPublished = currentShortcuts.any { it.id == channelId }

        if (currentShortcuts.size >= maxShortcutCount && !alreadyPublished) {
            val oldest = currentShortcuts
                .filterNot { it.id == channelId }
                .minByOrNull { shortcut -> shortcut.lastChangedTimestamp }

            oldest?.let { shortcut ->
                ShortcutManagerCompat.removeDynamicShortcuts(context, listOf(shortcut.id))
            }
        }

        ShortcutManagerCompat.addDynamicShortcuts(
            context,
            listOf(
                ShortcutInfoCompat.Builder(context, channelId)
                    .setIntent(intent)
                    .setLongLived(true)
                    .setIcon(icon)
                    .setShortLabel(channelName)
                    .setPerson(person)
                    .setIsConversation()
                    .build()
            )
        )
    }

    fun createGenericBubbleChannelIfNeeded(context: Context): NotificationChannelCompat? {
        with(NotificationManagerCompat.from(context)) {
            createNotificationChannel(
                NotificationChannelCompat.Builder(
                    NOTIFICATION_CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_HIGH
                )
                    .setName(context.getString(R.string.notification_channel_bubbles_title))
                    .setDescription(context.getString(R.string.notification_channel_bubbles_message))
                    .build()
            )

            return getNotificationChannelCompat(NOTIFICATION_CHANNEL_ID)
        }
    }

    fun createBubble(
        context: Context,
        user: User,
        icon: IconCompat,
        person: Person
    ): NotificationManagerCompat {
        return NotificationManagerCompat.from(context).apply {
            val intent = ChatActivity.createIntent(context, user.login)

            notify(
                notificationIdFor(user.id),
                NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(user.display_name)
                    .setContentIntent(
                        intent.toPendingIntent(context, mutable = false)
                    )
                    .setSmallIcon(R.drawable.ic_stream)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setLocusId(LocusIdCompat(user.id))
                    .setShortcutId(user.id)
                    .addPerson(person)
                    .setBubbleMetadata(
                        NotificationCompat.BubbleMetadata.Builder(
                            intent.toPendingIntent(context, mutable = true),
                            icon
                        )
                            .setAutoExpandBubble(true)
                            .setSuppressNotification(true)
                            .build()
                    )
                    .setStyle(
                        NotificationCompat.MessagingStyle(person)
                            .addMessage(
                                context.getString(R.string.notification_channel_bubbles_openPrompt),
                                System.currentTimeMillis(),
                                person
                            )
                    )
                    .build()
            )
        }
    }
}
