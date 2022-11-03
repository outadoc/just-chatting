package fr.outadoc.justchatting.ui.chat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.LocusIdCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import fr.outadoc.justchatting.ChatConnectionService
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.util.isLaunchedFromBubbleCompat
import fr.outadoc.justchatting.util.toPendingActivityIntent
import fr.outadoc.justchatting.util.toPendingForegroundServiceIntent


object ChatNotificationUtils {

    private const val NOTIFICATION_CHANNEL_ID = "channel_bubble"
    private const val KEY_QUICK_REPLY_TEXT = "quick_reply"

    private fun notificationIdFor(channelId: String) = channelId.hashCode()

    fun configureChatBubbles(context: Context, channel: User, channelLogo: Bitmap) {
        // Don't post a new notification if already in a bubble
        if ((context as? Activity)?.isLaunchedFromBubbleCompat == true) return

        // Bubbles are only available on Android Q+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        createGenericBubbleChannelIfNeeded(context) ?: return

        val icon = channelLogo.let { IconCompat.createWithBitmap(it) }

        val person: Person =
            Person.Builder()
                .setKey(channel.id)
                .setName(channel.displayName)
                .setIcon(icon)
                .build()

        createShortcutForChannel(
            context = context,
            intent = ChatActivity.createIntent(
                context = context,
                channelLogin = channel.login
            ),
            channelId = channel.id,
            channelName = channel.displayName,
            person = person,
            icon = icon
        )

        createBubble(
            context = context,
            user = channel,
            icon = icon,
            person = person
        )
    }

    private fun createShortcutForChannel(
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

    private fun createGenericBubbleChannelIfNeeded(context: Context): NotificationChannelCompat? {
        with(NotificationManagerCompat.from(context)) {
            createNotificationChannel(
                NotificationChannelCompat.Builder(
                    NOTIFICATION_CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_MIN
                )
                    .setName(context.getString(R.string.notification_channel_bubbles_title))
                    .setDescription(context.getString(R.string.notification_channel_bubbles_message))
                    .build()
            )

            return getNotificationChannelCompat(NOTIFICATION_CHANNEL_ID)
        }
    }

    private fun createBubble(
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
                    .setContentTitle(user.displayName)
                    .setContentIntent(intent.toPendingActivityIntent(context))
                    .setSmallIcon(R.drawable.ic_stream)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setLocusId(LocusIdCompat(user.id))
                    .setShortcutId(user.id)
                    .addPerson(person)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .addAction(
                        R.drawable.ic_close,
                        context.getString(R.string.notification_action_disconnect),
                        ChatConnectionService
                            .createStopIntent(context, channelId = user.id)
                            .toPendingForegroundServiceIntent(context)
                    )
                    .addAction(
                        NotificationCompat.Action.Builder(
                            R.drawable.ic_reply,
                            context.getString(R.string.notification_action_reply),
                            ChatConnectionService
                                .createReplyIntent(context, channelId = user.id)
                                .toPendingForegroundServiceIntent(context, mutable = true)
                        )
                            .addRemoteInput(
                                RemoteInput.Builder(KEY_QUICK_REPLY_TEXT)
                                    .setLabel(context.getString(R.string.notification_action_reply_hint))
                                    .build()
                            )
                            .build()
                    )
                    .setBubbleMetadata(
                        NotificationCompat.BubbleMetadata.Builder(
                            intent.toPendingActivityIntent(context, mutable = true),
                            icon
                        )
                            .setAutoExpandBubble(false)
                            .setSuppressNotification(false)
                            .setDeleteIntent(
                                ChatConnectionService
                                    .createStopIntent(context, channelId = user.id)
                                    .toPendingForegroundServiceIntent(context)
                            )
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

    fun dismissNotification(context: Context, channelId: String) {
        NotificationManagerCompat.from(context)
            .cancel(notificationIdFor(channelId))
    }
}
