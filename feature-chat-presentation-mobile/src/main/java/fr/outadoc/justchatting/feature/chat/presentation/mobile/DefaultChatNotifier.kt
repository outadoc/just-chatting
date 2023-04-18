package fr.outadoc.justchatting.feature.chat.presentation.mobile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.content.LocusIdCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.feature.chat.presentation.ChatConnectionService
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.getProfileImageIcon
import fr.outadoc.justchatting.utils.core.toPendingActivityIntent
import fr.outadoc.justchatting.utils.core.toPendingForegroundServiceIntent
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.ui.isLaunchedFromBubbleCompat

class DefaultChatNotifier : ChatNotifier {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "channel_bubble"
        private const val KEY_QUICK_REPLY_TEXT = "quick_reply"
    }

    override val supportsBackgroundChatService: Boolean = true

    private fun notificationIdFor(channelId: String) = channelId.hashCode()

    override fun notify(context: Context, user: User) {
        // Don't post a new notification if already in a bubble
        if ((context as? Activity)?.isLaunchedFromBubbleCompat == true) return

        createGenericBubbleChannelIfNeeded(context) ?: return

        val person: Person =
            Person.Builder()
                .setKey(user.id)
                .setName(user.displayName)
                .setIcon(user.getProfileImageIcon(context))
                .build()

        createShortcutForChannel(
            context = context,
            intent = ChatActivity.createIntent(
                context = context,
                channelLogin = user.login,
            ),
            user = user,
            person = person,
        )

        if (Build.VERSION.SDK_INT >= 26) {
            context.startForegroundService(
                ChatConnectionService.createStartIntent(context),
            )
        } else {
            context.startService(
                ChatConnectionService.createStartIntent(context),
            )
        }

        val notificationsPermissionCheck: Int =
            ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")

        when (notificationsPermissionCheck) {
            PackageManager.PERMISSION_GRANTED -> {
                // noinspection MissingPermission
                createNotificationForUser(
                    context = context,
                    user = user,
                    person = person,
                )
            }

            else -> {
                logError<DefaultChatNotifier> { "Notifications permission not granted (code: $notificationsPermissionCheck)" }
            }
        }
    }

    private fun createShortcutForChannel(
        context: Context,
        intent: Intent,
        user: User,
        person: Person,
    ) {
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

    private fun createGenericBubbleChannelIfNeeded(context: Context): NotificationChannelCompat? {
        val nm = NotificationManagerCompat.from(context)
        nm.createNotificationChannel(
            NotificationChannelCompat.Builder(
                NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_MIN,
            )
                .setName(context.getString(R.string.notification_channel_bubbles_title))
                .setDescription(context.getString(R.string.notification_channel_bubbles_message))
                .build(),
        )

        return nm.getNotificationChannelCompat(NOTIFICATION_CHANNEL_ID)
    }

    @RequiresPermission("android.permission.POST_NOTIFICATIONS")
    private fun createNotificationForUser(context: Context, user: User, person: Person) {
        val nm = NotificationManagerCompat.from(context)
        val intent = ChatActivity.createIntent(context, user.login)

        nm.notify(
            notificationIdFor(user.id),
            NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(user.displayName)
                .setContentIntent(intent.toPendingActivityIntent(context))
                .setSmallIcon(R.drawable.ic_notif)
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
                    ChatConnectionService.createStopIntent(context, channelId = user.id)
                        .toPendingForegroundServiceIntent(context),
                )
                .addAction(
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_reply,
                        context.getString(R.string.notification_action_reply),
                        ChatConnectionService.createReplyIntent(context, channelId = user.id)
                            .toPendingForegroundServiceIntent(context, mutable = true),
                    )
                        .addRemoteInput(
                            RemoteInput.Builder(KEY_QUICK_REPLY_TEXT)
                                .setLabel(context.getString(R.string.notification_action_reply_hint))
                                .build(),
                        )
                        .build(),
                )
                .setBubbleMetadata(
                    NotificationCompat.BubbleMetadata.Builder(
                        intent.toPendingActivityIntent(context, mutable = true),
                        user.getProfileImageIcon(context),
                    )
                        .setAutoExpandBubble(false)
                        .setSuppressNotification(false)
                        .setDeleteIntent(
                            ChatConnectionService.createStopIntent(context, channelId = user.id)
                                .toPendingForegroundServiceIntent(context),
                        )
                        .build(),
                )
                .setStyle(
                    NotificationCompat.MessagingStyle(person)
                        .addMessage(
                            context.getString(R.string.notification_channel_bubbles_openPrompt),
                            System.currentTimeMillis(),
                            person,
                        ),
                )
                .build(),
        )
    }

    override fun dismissNotification(context: Context, channelId: String) {
        NotificationManagerCompat.from(context)
            .cancel(notificationIdFor(channelId))
    }
}
