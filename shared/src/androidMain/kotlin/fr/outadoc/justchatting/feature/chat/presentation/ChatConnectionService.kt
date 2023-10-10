package fr.outadoc.justchatting.feature.chat.presentation

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.RemoteInput
import androidx.core.net.toUri
import fr.outadoc.justchatting.feature.chat.domain.ChatRepository
import fr.outadoc.justchatting.utils.logging.logInfo
import org.koin.android.ext.android.inject

class ChatConnectionService : Service() {

    companion object {
        private const val ACTION_REPLY = "reply"
        private const val KEY_QUICK_REPLY_TEXT = "quick_reply"

        fun createReplyIntent(context: Context, channelId: String): Intent {
            return Intent(context, ChatConnectionService::class.java).apply {
                data = "ccs://reply/?userId=$channelId".toUri()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logInfo<ChatConnectionService> { "Received intent $intent with data=${intent?.data}" }

        val connectionPool by inject<ChatRepository>()

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
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
