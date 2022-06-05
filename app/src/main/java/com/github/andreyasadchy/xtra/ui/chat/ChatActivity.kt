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
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
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

            val flags =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE
                else 0

            val bubbleIntent = PendingIntent.getActivity(context, 0, target, flags)

            val request = ImageRequest.Builder(context)
                .data(channelLogo)
                .size(512, 512)
                .target { drawable ->
                    val bitmap = drawable.toBitmap()

                    val person: Person =
                        Person.Builder()
                            .setKey(channelId)
                            .setName(channelName)
                            .build()

                    val notificationId = channelId.hashCode()

                    ShortcutManagerCompat.addDynamicShortcuts(
                        context, listOf(
                            ShortcutInfoCompat.Builder(context, channelId)
                                .setIntent(target)
                                .setLongLived(true)
                                .setShortLabel(channelName)
                                .build()
                        )
                    )

                    NotificationManagerCompat.from(context).apply {
                        createNotificationChannel(
                            NotificationChannelCompat.Builder(
                                CHANNEL_ID,
                                NotificationManagerCompat.IMPORTANCE_DEFAULT
                            )
                                .setName("Chat bubbles")
                                .build()
                        )

                        val icon = IconCompat.createWithBitmap(bitmap)

                        notify(
                            notificationId,
                            NotificationCompat.Builder(context, CHANNEL_ID)
                                .setContentIntent(bubbleIntent)
                                .setSmallIcon(R.drawable.ic_send_black_24dp)
                                .setBubbleMetadata(
                                    NotificationCompat.BubbleMetadata.Builder(bubbleIntent, icon)
                                        .setAutoExpandBubble(true)
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
                .build()

            context.imageLoader.enqueue(request)
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