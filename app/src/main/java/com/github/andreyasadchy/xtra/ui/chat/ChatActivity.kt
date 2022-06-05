package com.github.andreyasadchy.xtra.ui.chat

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
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.github.andreyasadchy.xtra.GlideApp
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.ui.main.BaseActivity
import com.github.andreyasadchy.xtra.util.C

class ChatActivity : BaseActivity() {

    companion object {

        private const val CHANNEL_ID = "channel_bubble"

        fun createIntent(
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

        fun openInBubble(
            context: Context,
            channelId: String,
            channelLogin: String,
            channelName: String,
            channelLogo: String
        ) {
            // Create bubble intent
            val target = createIntent(
                context = context,
                channelId = channelId,
                channelLogin = channelLogin,
                channelName = channelName,
                channelLogo = channelLogo
            )

            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE
                    else 0

            val bubbleIntent = PendingIntent.getActivity(context, 0, target, flags)

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

            createShortcutForChannel(context, target, channelId, channelName, person, icon)
            createGenericBubbleChannelIfNeeded(context)
            createBubbleChannelForUserIfNeeded(context, channelId, channelName)
            createBubble(context, channelId, bubbleIntent, icon, person)
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

        private fun createGenericBubbleChannelIfNeeded(context: Context) =
            NotificationManagerCompat.from(context).apply {
                createNotificationChannel(
                    NotificationChannelCompat.Builder(
                        CHANNEL_ID,
                        NotificationManagerCompat.IMPORTANCE_DEFAULT
                    )
                        // TODO extract to resource
                        .setName("Chat bubbles")
                        .build()
                )

            }

        private fun createBubbleChannelForUserIfNeeded(
            context: Context,
            channelId: String,
            channelName: String
        ) = NotificationManagerCompat.from(context).apply {
            createNotificationChannel(
                NotificationChannelCompat.Builder(
                    channelId,
                    NotificationManagerCompat.IMPORTANCE_DEFAULT
                )
                    .setName(channelName)
                    .setConversationId(CHANNEL_ID, channelId)
                    .build()
            )
        }

        private fun createBubble(
            context: Context,
            channelId: String,
            pendingIntent: PendingIntent,
            icon: IconCompat,
            person: Person
        ) = NotificationManagerCompat.from(context).apply {
            notify(
                notificationIdFor(channelId),
                NotificationCompat.Builder(context, channelId)
                    .setContentIntent(pendingIntent)
                    // TODO add some notification message
                    // TODO proper notification icon
                    .setSmallIcon(R.drawable.ic_send_black_24dp)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setBubbleMetadata(
                        NotificationCompat.BubbleMetadata.Builder(pendingIntent, icon)
                            .setAutoExpandBubble(true)
                            .setSuppressNotification(true)
                            .build()
                    )
                    .setLocusId(LocusIdCompat(channelId))
                    .setStyle(NotificationCompat.MessagingStyle(person))
                    .setShortcutId(channelId)
                    .addPerson(person)
                    .build()
            )
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