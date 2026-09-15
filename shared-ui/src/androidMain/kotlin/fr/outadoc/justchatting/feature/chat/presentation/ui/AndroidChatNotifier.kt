package fr.outadoc.justchatting.feature.chat.presentation.ui

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
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
import androidx.core.content.getSystemService
import fr.outadoc.justchatting.feature.chat.presentation.BubblePermission
import fr.outadoc.justchatting.feature.chat.presentation.ChatConnectionService
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.ProfileImageCache
import fr.outadoc.justchatting.feature.chat.presentation.getProfileImageIcon
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.shared.ui.R
import fr.outadoc.justchatting.utils.core.toPendingActivityIntent
import fr.outadoc.justchatting.utils.core.toPendingForegroundServiceIntent
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.logging.logInfo
import fr.outadoc.justchatting.utils.logging.logWarning
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

internal class AndroidChatNotifier(
    private val context: Context,
    private val preferenceRepository: PreferenceRepository,
    private val profileImageCache: ProfileImageCache,
) : ChatNotifier {
    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "channel_bubble_v2"

        /**
         * The original channel was created with `IMPORTANCE_MIN`, which made every bubble
         * notification "silent". Silent notifications are dropped by SystemUI's keyguard suppressor,
         * which also applies to bubbles, so a bubble posted while the device was locking would
         * vanish. Channel importance is immutable once created, hence the new id.
         */
        private const val LEGACY_NOTIFICATION_CHANNEL_ID = "channel_bubble"

        private const val KEY_QUICK_REPLY_TEXT = "quick_reply"

        /** How long to give the system to make up its mind before we check whether we got a bubble. */
        private const val BUBBLE_VERIFICATION_DELAY_MS = 750L

        /** How long to wait for the user's profile picture before posting without it. */
        private const val PROFILE_PICTURE_TIMEOUT_MS = 2_000L
    }

    // Without a handler, an uncaught throwable in a child coroutine still reaches the thread's
    // default handler and takes the process down: SupervisorJob only stops sibling cancellation.
    // Nothing here is worth crashing over — a missing bubble is the worst case.
    private val scope =
        CoroutineScope(
            SupervisorJob() +
                Dispatchers.Default +
                CoroutineExceptionHandler { _, throwable ->
                    logError<AndroidChatNotifier>(throwable) { "Failed to post chat notification" }
                },
        )

    private val notificationManager: NotificationManager?
        get() = context.getSystemService()

    // System-level state only; the enableNotifications app preference is checked
    // by notify(), which reads it off the main thread.
    override val areNotificationsEnabled: Boolean
        get() {
            val nm = NotificationManagerCompat.from(context)
            val notificationsPermissionCheck: Int =
                ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")

            return when (notificationsPermissionCheck) {
                PackageManager.PERMISSION_GRANTED -> {
                    nm.areNotificationsEnabled()
                }

                else -> {
                    logError<AndroidChatNotifier> {
                        "Notifications permission not granted (code: $notificationsPermissionCheck)"
                    }
                    false
                }
            }
        }

    override val bubblePermission: BubblePermission
        get() {
            if (Build.VERSION.SDK_INT < 29) return BubblePermission.Unsupported
            val nm = notificationManager ?: return BubblePermission.Unsupported

            return when {
                Build.VERSION.SDK_INT >= 31 -> {
                    when {
                        !nm.areBubblesEnabled() -> BubblePermission.Disabled
                        else ->
                            when (nm.bubblePreference) {
                                NotificationManager.BUBBLE_PREFERENCE_ALL -> BubblePermission.AllConversations
                                NotificationManager.BUBBLE_PREFERENCE_SELECTED -> BubblePermission.SelectedConversationsOnly
                                else -> BubblePermission.Disabled
                            }
                    }
                }

                else -> {
                    @Suppress("DEPRECATION")
                    if (nm.areBubblesAllowed()) {
                        BubblePermission.AllConversations
                    } else {
                        BubblePermission.Disabled
                    }
                }
            }
        }

    override fun notify(user: User) {
        scope.launch {
            val notificationsEnabledByUser: Boolean =
                preferenceRepository.currentPreferences
                    .first()
                    .enableNotifications

            if (!notificationsEnabledByUser) {
                logInfo<AndroidChatNotifier> { "Notifications disabled in app preferences, not notifying" }
                return@launch
            }

            if (!areNotificationsEnabled) return@launch

            createGenericBubbleChannelIfNeeded(context) ?: return@launch

            // Both the shortcut and the bubble icon are content URIs into
            // UserProfileImageContentProvider, which only ever serves what is already on disk.
            // Download the picture here, on our own coroutine, rather than leaving SystemUI to fall
            // back to the app icon. Bounded, because a bubble that shows up late is worse than one
            // wearing the wrong icon; the download carries on regardless and will be there next
            // time.
            withTimeoutOrNull(PROFILE_PICTURE_TIMEOUT_MS) {
                profileImageCache.fetch(user.id)
            }

            // The system resolves the notification's shortcut id synchronously while enqueuing it,
            // and silently strips the bubble metadata when it finds nothing. Publish first.
            publishConversationShortcut(context = context, user = user)

            // noinspection MissingPermission
            createNotificationForUser(context, user)

            delay(BUBBLE_VERIFICATION_DELAY_MS)

            if (!isShowingAsBubble(user)) {
                logBubbleDiagnosis(user)

                // Only worth another go when the system would allow a bubble at all: the shortcut
                // may just have become visible to the notification service, or the device may have
                // been mid-lock. Reposting is cheap and idempotent (same notification id).
                if (bubblePermission == BubblePermission.AllConversations) {
                    publishConversationShortcut(context = context, user = user)

                    // noinspection MissingPermission
                    createNotificationForUser(context, user)
                }
            }
        }
    }

    private fun createGenericBubbleChannelIfNeeded(context: Context): NotificationChannelCompat? {
        val nm = NotificationManagerCompat.from(context)

        if (nm.getNotificationChannelCompat(LEGACY_NOTIFICATION_CHANNEL_ID) != null) {
            nm.deleteNotificationChannel(LEGACY_NOTIFICATION_CHANNEL_ID)
        }

        nm.createNotificationChannel(
            NotificationChannelCompat
                .Builder(
                    NOTIFICATION_CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_LOW,
                ).setName(context.getString(R.string.notification_channel_bubbles_title))
                .setDescription(context.getString(R.string.notification_channel_bubbles_message))
                .build(),
        )

        return nm.getNotificationChannelCompat(NOTIFICATION_CHANNEL_ID)
    }

    @RequiresPermission("android.permission.POST_NOTIFICATIONS")
    private fun createNotificationForUser(
        context: Context,
        user: User,
    ) {
        val nm = NotificationManagerCompat.from(context)
        val intent = EmbeddedChatActivity.createIntent(context, user.id)

        val person =
            Person
                .Builder()
                .setKey(user.id)
                .setName(user.displayName)
                .setIcon(user.getProfileImageIcon(context))
                .build()

        nm.notify(
            notificationIdFor(user.id),
            NotificationCompat
                .Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(user.displayName)
                .setContentIntent(intent.toPendingActivityIntent(context))
                .setSmallIcon(R.drawable.ic_notif)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setLocusId(LocusIdCompat(user.id))
                .setShortcutId(user.id)
                .addPerson(person)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .addAction(
                    NotificationCompat.Action
                        .Builder(
                            R.drawable.ic_reply,
                            context.getString(R.string.notification_action_reply),
                            ChatConnectionService
                                .createReplyIntent(context, channelId = user.id)
                                .toPendingForegroundServiceIntent(context, mutable = true),
                        ).setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                        .addRemoteInput(
                            RemoteInput
                                .Builder(KEY_QUICK_REPLY_TEXT)
                                .setLabel(context.getString(R.string.notification_action_reply_hint))
                                .build(),
                        ).build(),
                ).setBubbleMetadata(
                    NotificationCompat.BubbleMetadata
                        .Builder(
                            intent.toPendingActivityIntent(context, mutable = true),
                            user.getProfileImageIcon(context),
                        ).setDesiredHeightResId(R.dimen.height_bubbleWindow)
                        .setAutoExpandBubble(false)
                        .setSuppressNotification(true)
                        .build(),
                ).setStyle(
                    NotificationCompat
                        .MessagingStyle(person)
                        .addMessage(
                            context.getString(R.string.notification_channel_bubbles_openPrompt),
                            System.currentTimeMillis(),
                            person,
                        ),
                ).build(),
        )
    }

    /**
     * Whether the notification we just posted for [user] actually ended up flagged as a bubble.
     *
     * The system never tells an app that it declined to bubble a notification, so we read our own
     * notification back and look at the flag it came away with.
     */
    private fun isShowingAsBubble(user: User): Boolean {
        val id = notificationIdFor(user.id)
        return NotificationManagerCompat
            .from(context)
            .activeNotifications
            .any { posted -> posted.id == id && (posted.notification.flags and Notification.FLAG_BUBBLE) != 0 }
    }

    private fun logBubbleDiagnosis(user: User) {
        val keyguardManager: KeyguardManager? = context.getSystemService()

        logWarning<AndroidChatNotifier> {
            "Notification for ${user.displayName} (${user.id}) did not become a bubble. " +
                "bubblePermission=$bubblePermission, " +
                "hasConversationShortcut=${hasValidConversationShortcut(context, user.id)}, " +
                "keyguardLocked=${keyguardManager?.isKeyguardLocked}, " +
                "notificationsEnabled=$areNotificationsEnabled"
        }
    }

    private fun notificationIdFor(channelId: String) = channelId.hashCode()
}
