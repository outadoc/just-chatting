package fr.outadoc.justchatting.feature.chat.presentation

import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.utils.logging.logInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

internal class ChatConnectionService : LifecycleService() {
    companion object {
        private const val ACTION_REPLY = "reply"
        private const val KEY_QUICK_REPLY_TEXT = "quick_reply"

        fun createReplyIntent(
            context: Context,
            channelId: String,
        ): Intent = Intent(context, ChatConnectionService::class.java).apply {
            data = "ccs://reply/?userId=$channelId".toUri()
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        logInfo<ChatConnectionService> { "Received intent $intent with data=${intent?.data}" }

        val repository by inject<TwitchRepository>()
        val authRepository by inject<AuthRepository>()

        val action: String? = intent?.data?.authority
        val userId: String? = intent?.data?.getQueryParameter("userId")

        logInfo<ChatConnectionService> { "action=$action, userId=$userId" }

        when (action) {
            ACTION_REPLY -> {
                lifecycleScope.launch {
                    val quickReplyResult: String? =
                        RemoteInput
                            .getResultsFromIntent(intent)
                            ?.getCharSequence(KEY_QUICK_REPLY_TEXT)
                            ?.toString()

                    logInfo<ChatConnectionService> { "Replying to $userId's chat with reply: $quickReplyResult" }

                    if (userId != null && quickReplyResult != null) {
                        val appUser = authRepository.currentUser.first()
                        repository.sendChatMessage(
                            channelUserId = userId,
                            message = quickReplyResult,
                            inReplyToMessageId = null,
                            appUser = appUser,
                        )
                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }
}
