package fr.outadoc.justchatting.feature.chat.presentation

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.net.toUri
import fr.outadoc.justchatting.feature.chat.domain.ChatRepository
import fr.outadoc.justchatting.utils.logging.logInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ChatConnectionService : Service() {

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 457542
        private const val ONGOING_NOTIFICATION_CHANNEL_ID = "background_channel"

        private const val ACTION_STOP = "stop"
        private const val ACTION_REPLY = "reply"

        private const val KEY_QUICK_REPLY_TEXT = "quick_reply"

        fun createStartIntent(context: Context): Intent {
            return Intent(context, ChatConnectionService::class.java).apply {
                data = "ccs://start".toUri()
            }
        }

        fun createStopIntent(context: Context, channelId: String): Intent {
            return Intent(context, ChatConnectionService::class.java).apply {
                data = "ccs://stop/?userId=$channelId".toUri()
            }
        }

        fun createReplyIntent(context: Context, channelId: String): Intent {
            return Intent(context, ChatConnectionService::class.java).apply {
                data = "ccs://reply/?userId=$channelId".toUri()
            }
        }
    }

    private val chatNotifier: ChatNotifier by inject()

    private val connectionPool: ChatRepository by inject()

    private lateinit var job: Job
    private val coroutineScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.Default + job)

    override fun onCreate() {
        super.onCreate()

        postForegroundNotification()

        job = Job()

        coroutineScope.launch {
            connectionPool.connectionStatus.collect { status ->
                if (status.registeredListeners < 1) {
                    logInfo<ChatConnectionService> { "Pool has no active threads left, stopping background service" }

                    if (Build.VERSION.SDK_INT >= 24) {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                    } else {
                        @Suppress("DEPRECATION")
                        stopForeground(true)
                    }

                    stopSelf()
                } else {
                    logInfo<ChatConnectionService> { "Pool still has active threads left, service will keep running" }
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logInfo<ChatConnectionService> { "Received intent $intent with data=${intent?.data}" }

        postForegroundNotification()

        val action: String? = intent?.data?.authority
        val userId: String? = intent?.data?.getQueryParameter("userId")

        logInfo<ChatConnectionService> { "action=$action, userId=$userId" }

        when (action) {
            ACTION_REPLY -> {
                val quickReplyResult: CharSequence? =
                    RemoteInput.getResultsFromIntent(intent)
                        ?.getCharSequence(KEY_QUICK_REPLY_TEXT)

                logInfo<ChatConnectionService> { "Replying to $userId's chat with reply: $quickReplyResult" }

                if (userId != null && quickReplyResult != null) {
                    connectionPool.sendMessage(
                        channelId = userId,
                        message = quickReplyResult,
                    )
                }
            }

            ACTION_STOP -> {
                logInfo<ChatConnectionService> { "Stopping thread for $userId" }

                if (userId != null) {
                    connectionPool.stop(userId)
                    chatNotifier.dismissNotification(
                        context = this,
                        channelId = userId,
                    )
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun postForegroundNotification() {
        NotificationManagerCompat.from(this)
            .createNotificationChannel(
                NotificationChannelCompat.Builder(
                    ONGOING_NOTIFICATION_CHANNEL_ID,
                    NotificationManagerCompat.IMPORTANCE_MIN,
                )
                    .setName(getString(R.string.notification_foreground_channel_title))
                    .setDescription(getString(R.string.notification_foreground_channel_message))
                    .build(),
            )

        val notification = NotificationCompat.Builder(this, ONGOING_NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_foreground_title))
            .setContentText(getString(R.string.notification_foreground_message))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(getString(R.string.notification_foreground_message)),
            )
            .setSmallIcon(R.drawable.ic_notif)
            .setOngoing(true)
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionPool.dispose()
        job.cancel()
    }
}
