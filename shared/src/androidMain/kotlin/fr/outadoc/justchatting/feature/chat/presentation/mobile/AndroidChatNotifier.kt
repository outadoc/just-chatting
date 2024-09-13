package fr.outadoc.justchatting.feature.chat.presentation.mobile

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.content.LocusIdCompat
import fr.outadoc.justchatting.feature.chat.presentation.ChatConnectionService
import fr.outadoc.justchatting.feature.chat.presentation.ChatNotifier
import fr.outadoc.justchatting.feature.chat.presentation.getProfileImageIcon
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.shared.R
import fr.outadoc.justchatting.utils.core.toPendingActivityIntent
import fr.outadoc.justchatting.utils.core.toPendingForegroundServiceIntent
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.presentation.isLaunchedFromBubbleCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal class AndroidChatNotifier(
    private val context: Context,
    private val preferenceRepository: PreferenceRepository,
) : ChatNotifier {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "channel_bubble"
        private const val KEY_QUICK_REPLY_TEXT = "quick_reply"
    }

    override val areNotificationsEnabled: Boolean
        get() {
            val nm = NotificationManagerCompat.from(context)
            val notificationsPermissionCheck: Int =
                ContextCompat.checkSelfPermission(context, "android.permission.POST_NOTIFICATIONS")

            val currentPrefs: AppPreferences =
                runBlocking { preferenceRepository.currentPreferences.first() }

            return when (notificationsPermissionCheck) {
                PackageManager.PERMISSION_GRANTED -> {
                    nm.areNotificationsEnabled() && currentPrefs.enableNotifications
                }

                else -> {
                    logError<AndroidChatNotifier> {
                        "Notifications permission not granted (code: $notificationsPermissionCheck)"
                    }
                    false
                }
            }
        }

    override fun notify(context: Context, user: User) {
        // Don't post a new notification if already in a bubble
        if ((context as? Activity)?.isLaunchedFromBubbleCompat == true) return

        if (areNotificationsEnabled) {
            createGenericBubbleChannelIfNeeded(context) ?: return

            // noinspection MissingPermission
            createNotificationForUser(context, user)
        }
    }

    private fun createGenericBubbleChannelIfNeeded(context: Context): NotificationChannelCompat? {
        val nm = NotificationManagerCompat.from(context)
        nm.createNotificationChannel(
            NotificationChannelCompat.Builder(
                NOTIFICATION_CHANNEL_ID,
                NotificationManagerCompat.IMPORTANCE_MIN,
            )
                .setName(MR.strings.notification_channel_bubbles_title.getString(context))
                .setDescription(MR.strings.notification_channel_bubbles_message.getString(context))
                .build(),
        )

        return nm.getNotificationChannelCompat(NOTIFICATION_CHANNEL_ID)
    }

    @RequiresPermission("android.permission.POST_NOTIFICATIONS")
    private fun createNotificationForUser(context: Context, user: User) {
        val nm = NotificationManagerCompat.from(context)
        val intent = ChatActivity.createIntent(context, user.id)

        val person = Person.Builder()
            .setKey(user.id)
            .setName(user.displayName)
            .setIcon(user.getProfileImageIcon(context))
            .build()

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
                .addAction(
                    NotificationCompat.Action.Builder(
                        R.drawable.ic_reply,
                        MR.strings.notification_action_reply.getString(context),
                        ChatConnectionService.createReplyIntent(context, channelId = user.id)
                            .toPendingForegroundServiceIntent(context, mutable = true),
                    )
                        .setSemanticAction(NotificationCompat.Action.SEMANTIC_ACTION_REPLY)
                        .addRemoteInput(
                            RemoteInput.Builder(KEY_QUICK_REPLY_TEXT)
                                .setLabel(
                                    MR.strings.notification_action_reply_hint.getString(
                                        context,
                                    ),
                                )
                                .build(),
                        )
                        .build(),
                )
                .setBubbleMetadata(
                    NotificationCompat.BubbleMetadata.Builder(
                        intent.toPendingActivityIntent(context, mutable = true),
                        user.getProfileImageIcon(context),
                    )
                        .setDesiredHeightResId(R.dimen.height_bubbleWindow)
                        .setAutoExpandBubble(false)
                        .setSuppressNotification(true)
                        .build(),
                )
                .setStyle(
                    NotificationCompat.MessagingStyle(person)
                        .addMessage(
                            MR.strings.notification_channel_bubbles_openPrompt.getString(context),
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

    private fun notificationIdFor(channelId: String) = channelId.hashCode()
}
