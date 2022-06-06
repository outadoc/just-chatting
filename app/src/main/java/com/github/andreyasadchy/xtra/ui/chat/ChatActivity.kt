package com.github.andreyasadchy.xtra.ui.chat

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.LocusIdCompat
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.github.andreyasadchy.xtra.GlideApp
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.main.BaseActivity
import com.github.andreyasadchy.xtra.util.C

class ChatActivity : BaseActivity() {

    companion object {

        private const val NOTIFICATION_CHANNEL_ID = "channel_bubble"

        fun openInBubbleOrStartActivity(
            context: Context,
            channelId: String,
            channelLogin: String,
            channelName: String,
            channelLogo: String
        ) {
            val channel = createGenericBubbleChannelIfNeeded(context)
            if (channel != null && areBubblesAllowed(context, channel)) {
                openInBubble(context, channelId, channelLogin, channelName, channelLogo)
            } else {
                startActivity(context, channelId, channelLogin, channelName, channelLogo)
            }
        }

        private fun areBubblesAllowed(
            context: Context,
            channel: NotificationChannelCompat
        ): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
            if (channel.canBubble()) return true
            val nm = context.getSystemService<NotificationManager>() ?: return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                nm.bubblePreference != NotificationManager.BUBBLE_PREFERENCE_NONE
            ) return true

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && nm.areBubblesAllowed())
                return true

            return false
        }

        private fun startActivity(
            context: Context,
            channelId: String,
            channelLogin: String,
            channelName: String,
            channelLogo: String
        ) {
            context.startActivity(
                createIntent(
                    context = context,
                    channelId = channelId,
                    channelLogin = channelLogin,
                    channelName = channelName,
                    channelLogo = channelLogo
                )
            )
        }

        private fun openInBubble(
            context: Context,
            channelId: String,
            channelLogin: String,
            channelName: String,
            channelLogo: String
        ) {
            val request = GlideApp.with(context)
                .asBitmap()
                .load(channelLogo)

            val icon = IconCompat.createWithAdaptiveBitmap(request.submit().get())

            val person: Person =
                Person.Builder()
                    .setKey(channelId)
                    .setName(channelName)
                    .setIcon(icon)
                    .build()

            createShortcutForChannel(
                context = context,
                intent = createIntent(
                    context = context,
                    channelId = channelId,
                    channelLogin = channelLogin,
                    channelName = channelName,
                    channelLogo = channelLogo
                ),
                channelId = channelId,
                channelName = channelName,
                person = person,
                icon = icon
            )

            createBubble(
                context = context,
                channelId = channelId,
                channelLogin = channelLogin,
                channelName = channelName,
                channelLogo = channelLogo,
                icon = icon,
                person = person
            )
        }

        private fun notificationIdFor(channelId: String) = channelId.hashCode()

        private fun createShortcutForChannel(
            context: Context,
            intent: Intent,
            channelId: String,
            channelName: String,
            person: Person,
            icon: IconCompat
        ) {
            ShortcutManagerCompat.addDynamicShortcuts(
                context, listOf(
                    ShortcutInfoCompat.Builder(context, channelId)
                        .setIntent(intent)
                        .setLongLived(true)
                        .setIcon(icon)
                        .setShortLabel(channelName)
                        .setPerson(person)
                        .build()
                )
            )
        }

        private fun createGenericBubbleChannelIfNeeded(context: Context): NotificationChannelCompat? {
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

        private fun createBubble(
            context: Context,
            channelId: String,
            channelLogin: String,
            channelName: String,
            channelLogo: String,
            icon: IconCompat,
            person: Person
        ) = NotificationManagerCompat.from(context).apply {
            notify(
                notificationIdFor(channelId),
                NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(channelName)
                    .setContentIntent(
                        createPendingIntent(
                            context = context,
                            channelId = channelId,
                            channelLogin = channelLogin,
                            channelName = channelName,
                            channelLogo = channelLogo,
                            mutable = false
                        )
                    )
                    .setSmallIcon(R.drawable.ic_stream)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setLocusId(LocusIdCompat(channelId))
                    .setShortcutId(channelId)
                    .addPerson(person)
                    .setBubbleMetadata(
                        NotificationCompat.BubbleMetadata.Builder(
                            createPendingIntent(
                                context = context,
                                channelId = channelId,
                                channelLogin = channelLogin,
                                channelName = channelName,
                                channelLogo = channelLogo,
                                mutable = true
                            ),
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

        private fun createPendingIntent(
            context: Context,
            channelId: String,
            channelLogin: String,
            channelName: String,
            channelLogo: String,
            mutable: Boolean
        ): PendingIntent {
            val mutableFlag =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && mutable) PendingIntent.FLAG_MUTABLE
                else 0

            val immutableFlag =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !mutable) PendingIntent.FLAG_IMMUTABLE
                else 0

            return PendingIntent.getActivity(
                context,
                0,
                createIntent(context, channelId, channelLogin, channelName, channelLogo),
                PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag or immutableFlag
            )
        }

        private fun createIntent(
            context: Context,
            channelId: String,
            channelLogin: String,
            channelName: String,
            channelLogo: String
        ): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

                putExtra(C.CHANNEL_ID, channelId)
                putExtra(C.CHANNEL_LOGIN, channelLogin)
                putExtra(C.CHANNEL_DISPLAYNAME, channelName)
                putExtra(C.CHANNEL_PROFILEIMAGE, channelLogo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager
            .beginTransaction()
            .replace(
                android.R.id.content,
                ChannelChatFragment.newInstance(
                    id = intent.getStringExtra(C.CHANNEL_ID),
                    login = intent.getStringExtra(C.CHANNEL_LOGIN),
                    name = intent.getStringExtra(C.CHANNEL_DISPLAYNAME),
                    channelLogo = intent.getStringExtra(C.CHANNEL_PROFILEIMAGE)
                )
            )
            .commit()
    }
}