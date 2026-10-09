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
         * Was `IMPORTANCE_MIN`, which let the keyguard suppressor drop bubbles. Channel importance
         * is immutable once created, hence the new id.
         */
        private const val LEGACY_NOTIFICATION_CHANNEL_ID = "channel_bubble"

        private const val KEY_QUICK_REPLY_TEXT = "quick_reply"

        /** How long to let the system settle before reading the notification back. */
        private const val BUBBLE_VERIFICATION_DELAY_MS = 750L

        /** How long to wait for the profile picture before posting without it. */
        private const val PROFILE_PICTURE_TIMEOUT_MS = 2_000L
    }

    // SupervisorJob only stops sibling cancellation; without a handler an uncaught throwable
    // would still take the process down.
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

    // System-level state only; the enableNotifications preference is checked by notify().
    override val areNotificationsEnabled: Boolean
        get() {
            val nm = NotificationManagerCompat.from(context)
            val notificationsPermissionCheck: Int =
                ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")

            return when (notificationsPermissionCheck) {
                PackageManager.PERMISSION_GRANTED -> {
                    nm.areNotificationsEnabled().also { enabled ->
                        if (!enabled) {
                            logWarning<AndroidChatNotifier> {
                                "Notifications are turned off for the app in system settings"
                            }
                        }
                    }
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
            logInfo<AndroidChatNotifier> { "Notifying for ${user.displayName} (${user.id})" }

            val notificationsEnabledByUser: Boolean =
                preferenceRepository.currentPreferences
                    .first()
                    .enableNotifications

            if (!notificationsEnabledByUser) {
                logInfo<AndroidChatNotifier> { "Notifications disabled in app preferences, not notifying" }
                return@launch
            }

            // areNotificationsEnabled logs why it said no.
            if (!areNotificationsEnabled) return@launch

            if (createGenericBubbleChannelIfNeeded(context) == null) {
                logError<AndroidChatNotifier> {
                    "Could not create the $NOTIFICATION_CHANNEL_ID channel, not notifying"
                }
                return@launch
            }

            // The icons are content URIs that only serve what is already on disk. Bounded: a late
            // bubble is worse than one wearing the app icon, and the download continues regardless.
            withTimeoutOrNull(PROFILE_PICTURE_TIMEOUT_MS) {
                profileImageCache.fetch(user.id)
            }

            // The system resolves the notification's shortcut id synchronously while enqueuing it,
            // and silently strips the bubble metadata when it finds nothing. Publish first.
            publishConversationShortcut(context = context, user = user)

            // Reposting an identical notification is not "visually interruptive", and SystemUI
            // leaves a bubble that sits in the overflow (dismissed by the user, or evicted once the
            // stack is full) where it is, while still reporting FLAG_BUBBLE. Only a fresh post
            // brings it back, so drop the previous one first.
            if (readBackOutcome(user) != PostOutcome.Missing) {
                logInfo<AndroidChatNotifier> {
                    "Replacing existing notification for ${user.displayName} (${user.id})"
                }
                NotificationManagerCompat.from(context).cancel(notificationIdFor(user.id))
            }

            // noinspection MissingPermission
            createNotificationForUser(context, user)

            delay(BUBBLE_VERIFICATION_DELAY_MS)

            val outcome: PostOutcome = readBackOutcome(user)
            logBubbleDiagnosis(user, outcome)

            if (outcome != PostOutcome.Bubbled) {
                // The shortcut may only now have become visible to the notification service, or
                // the device may have been mid-lock. Reposting is idempotent (same id).
                if (bubblePermission == BubblePermission.AllConversations) {
                    publishConversationShortcut(context = context, user = user)

                    // noinspection MissingPermission
                    createNotificationForUser(context, user)

                    delay(BUBBLE_VERIFICATION_DELAY_MS)

                    logInfo<AndroidChatNotifier> {
                        "Reposted notification for ${user.displayName} (${user.id}), " +
                            "outcome=${readBackOutcome(user)}"
                    }
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
     * What became of the notification we just posted, as far as an app can tell.
     *
     * [Bubbled] is not proof the user saw a bubble: `FLAG_BUBBLE` only means the notification
     * service permitted one. Whether SystemUI shows it, overflows it or drops it is unobservable.
     */
    private enum class PostOutcome {
        /** Not there at all: the post did not take. */
        Missing,

        /** Posted, but the system declined to bubble it and stripped the metadata. */
        NotBubbled,

        /** Posted, and the system allowed it to bubble. */
        Bubbled,
    }

    private fun readBackOutcome(user: User): PostOutcome {
        val id = notificationIdFor(user.id)
        val posted =
            NotificationManagerCompat
                .from(context)
                .activeNotifications
                .firstOrNull { notification -> notification.id == id }
                ?: return PostOutcome.Missing

        return if ((posted.notification.flags and Notification.FLAG_BUBBLE) != 0) {
            PostOutcome.Bubbled
        } else {
            PostOutcome.NotBubbled
        }
    }

    private fun logBubbleDiagnosis(
        user: User,
        outcome: PostOutcome,
    ) {
        val keyguardManager: KeyguardManager? = context.getSystemService()

        val diagnosis =
            "outcome=$outcome, " +
                "bubblePermission=$bubblePermission, " +
                "hasConversationShortcut=${hasValidConversationShortcut(context, user.id)}, " +
                "keyguardLocked=${keyguardManager?.isKeyguardLocked}, " +
                "notificationsEnabled=$areNotificationsEnabled"

        // Logged either way: a recorded Bubbled rules out everything up to the notification service.
        if (outcome == PostOutcome.Bubbled) {
            logInfo<AndroidChatNotifier> {
                "Posted notification for ${user.displayName} (${user.id}). $diagnosis"
            }
        } else {
            logWarning<AndroidChatNotifier> {
                "Notification for ${user.displayName} (${user.id}) did not become a bubble. $diagnosis"
            }
        }
    }

    private fun notificationIdFor(channelId: String) = channelId.hashCode()
}
