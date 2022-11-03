package fr.outadoc.justchatting

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.outadoc.justchatting.repository.ChatConnectionPool
import fr.outadoc.justchatting.ui.chat.ChatNotificationUtils
import org.koin.android.ext.android.inject

class ChatConnectionService : Service() {

    companion object {
        const val TAG = "ChatConnectionService"

        private const val ONGOING_NOTIFICATION_ID = 457542
        private const val ONGOING_NOTIFICATION_CHANNEL_ID = "background_channel"

        private const val ACTION_STOP = "ACTION_STOP"
        private const val EXTRA_CHANNEL_ID = "EXTRA_CHANNEL_ID"

        fun createStartIntent(context: Context): Intent {
            return Intent(context, ChatConnectionService::class.java)
        }

        fun createStopIntent(context: Context, channelId: String): Intent {
            return Intent(context, ChatConnectionService::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_CHANNEL_ID, channelId)
            }
        }
    }

    private val connectionPool: ChatConnectionPool by inject()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Received intent $intent")

        when (intent?.action) {
            ACTION_STOP -> {
                intent.getStringExtra(EXTRA_CHANNEL_ID)?.let { channelId ->
                    Log.i(TAG, "Stopping thread for $channelId")
                    connectionPool.stop(channelId)
                    ChatNotificationUtils.dismissNotification(
                        context = this,
                        channelId = channelId
                    )
                }

                if (!connectionPool.hasActiveThreads) {
                    Log.i(TAG, "Pool has no active threads left, stopping background service")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    Log.i(TAG, "Pool still has active threads left, service will keep running")
                }
            }

            else -> {
                NotificationManagerCompat.from(this)
                    .createNotificationChannel(
                        NotificationChannelCompat.Builder(
                            ONGOING_NOTIFICATION_CHANNEL_ID,
                            NotificationManagerCompat.IMPORTANCE_LOW
                        )
                            .setName(getString(R.string.notification_foreground_channel_title))
                            .setDescription(getString(R.string.notification_foreground_channel_message))
                            .build()
                    )

                val notification = NotificationCompat.Builder(this, ONGOING_NOTIFICATION_CHANNEL_ID)
                    .setContentTitle(getString(R.string.notification_foreground_title))
                    .setContentText(getString(R.string.notification_foreground_message))
                    .setSmallIcon(R.drawable.ic_campaign)
                    .setOngoing(true)
                    .build()

                startForeground(ONGOING_NOTIFICATION_ID, notification)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        connectionPool.dispose()
    }
}
