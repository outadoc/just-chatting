package fr.outadoc.justchatting.util.chat

import android.content.Context
import android.util.Log
import fr.outadoc.justchatting.irc.ChatMessageParser
import fr.outadoc.justchatting.model.chat.Command
import fr.outadoc.justchatting.model.chat.PingCommand
import fr.outadoc.justchatting.model.chat.UserState
import fr.outadoc.justchatting.repository.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.io.IOException

/**
 * Logged in chat thread.
 *
 * Needed because user's own messages are only send when logged out. This thread handles
 * user-specific NOTICE and USERSTATE messages, and [LiveChatWebSocket] handles the rest.
 *
 * Use this class to write messages to the chat.
 */
class LoggedInChatWebSocket(
    applicationContext: Context,
    private val scope: CoroutineScope,
    private val clock: Clock,
    private val parser: ChatMessageParser,
    private val userPreferencesRepository: UserPreferencesRepository,
    channelLogin: String
) : BaseChatWebSocket(applicationContext, scope, clock, channelLogin) {

    class Factory(
        private val applicationContext: Context,
        private val clock: Clock,
        private val parser: ChatMessageParser,
        private val userPreferencesRepository: UserPreferencesRepository
    ) {
        fun create(scope: CoroutineScope, channelLogin: String): LoggedInChatWebSocket {
            return LoggedInChatWebSocket(
                applicationContext = applicationContext,
                clock = clock,
                parser = parser,
                scope = scope,
                channelLogin = channelLogin,
                userPreferencesRepository = userPreferencesRepository
            )
        }
    }

    fun start() {
        connect(socketListener = LiveChatThreadListener())
    }

    private inner class LiveChatThreadListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            scope.launch {
                val user = userPreferencesRepository.appUser.first()
                with(webSocket) {
                    send("PASS oauth:${user.helixToken}")
                    send("NICK ${user.login}")
                    send("CAP REQ :twitch.tv/tags twitch.tv/commands")
                    send("JOIN $hashChannelName")
                }

                Log.d(TAG, "Successfully logged in to $hashChannelName")
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            text.lines()
                .filter { message -> message.isNotBlank() }
                .forEach(::notifyMessage)
        }

        private fun notifyMessage(message: String) {
            when (val command = parser.parse(message)) {
                is Command.Notice, is UserState -> emit(command)
                PingCommand -> sendPong()
                else -> {}
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            t.printStackTrace()
            attemptReconnect(listener = this@LiveChatThreadListener)
        }
    }

    fun send(message: CharSequence, inReplyToId: String?) {
        try {
            val inReplyToPrefix = inReplyToId?.let { id -> "@reply-parent-msg-id=$id " } ?: ""
            val privMsg = "${inReplyToPrefix}PRIVMSG $hashChannelName :$message"

            socket?.send(privMsg)

            Log.d(TAG, "Sent message to $hashChannelName, in reply to $inReplyToId: $message")
        } catch (e: IOException) {
            Log.e(TAG, "Error sending message", e)
            emit(
                Command.SendMessageError(
                    throwable = e,
                    timestamp = clock.now()
                )
            )
        }
    }
}
