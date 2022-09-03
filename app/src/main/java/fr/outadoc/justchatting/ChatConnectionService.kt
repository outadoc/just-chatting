package fr.outadoc.justchatting

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.outadoc.justchatting.repository.ChatConnectionPool
import fr.outadoc.justchatting.ui.main.MainActivity
import org.koin.android.ext.android.inject

class ChatConnectionService : Service() {

    companion object {
        private const val ONGOING_NOTIFICATION_ID = 457542
        private const val ONGOING_NOTIFICATION_CHANNEL_ID = "background_channel"

        private const val ACTION_STOP = "ACTION_STOP"

        fun createStartIntent(context: Context): Intent {
            return Intent(context, ChatConnectionService::class.java)
        }

        fun createStopIntent(context: Context): Intent {
            return Intent(context, ChatConnectionService::class.java).apply {
                action = ACTION_STOP
            }
        }
    }

    private val multiplexer: ChatConnectionPool by inject()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(true)
            stopSelf()
            return super.onStartCommand(intent, flags, startId)
        }

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
            .addAction(
                R.drawable.ic_mood,
                getString(R.string.notification_foreground_action),
                createStopIntent(this).toPendingIntent(this)
            )
            .setContentIntent(
                MainActivity.createIntent(this).toPendingIntent(this)
            )
            .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        multiplexer.dispose()
    }

    private fun Intent.toPendingIntent(
        context: Context,
        mutable: Boolean = false
    ): PendingIntent {
        val mutableFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && mutable) PendingIntent.FLAG_MUTABLE
            else 0

        val immutableFlag =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !mutable) PendingIntent.FLAG_IMMUTABLE
            else 0

        return PendingIntent.getForegroundService(
            context,
            0,
            this,
            PendingIntent.FLAG_UPDATE_CURRENT or mutableFlag or immutableFlag
        )
    }
}
