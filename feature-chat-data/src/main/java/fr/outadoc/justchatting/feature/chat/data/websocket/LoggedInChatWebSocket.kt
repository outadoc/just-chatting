package fr.outadoc.justchatting.feature.chat.data.websocket

import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.chat.data.ChatCommandHandlerFactory
import fr.outadoc.justchatting.feature.chat.data.ConnectionStatus
import fr.outadoc.justchatting.feature.chat.data.model.Command
import fr.outadoc.justchatting.feature.chat.data.model.PingCommand
import fr.outadoc.justchatting.feature.chat.data.model.UserState
import fr.outadoc.justchatting.feature.chat.data.parser.ChatMessageParser
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
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
    networkStateObserver: NetworkStateObserver,
    private val scope: CoroutineScope,
    private val clock: Clock,
    private val parser: ChatMessageParser,
    private val preferencesRepository: PreferenceRepository,
    channelLogin: String
) : BaseChatWebSocket(networkStateObserver, scope, clock, channelLogin) {

    class Factory(
        private val networkStateObserver: NetworkStateObserver,
        private val clock: Clock,
        private val parser: ChatMessageParser,
        private val preferencesRepository: PreferenceRepository,
    ) : ChatCommandHandlerFactory {

        override fun create(
            scope: CoroutineScope,
            channelLogin: String,
            channelId: String
        ): LoggedInChatWebSocket {
            return LoggedInChatWebSocket(
                networkStateObserver = networkStateObserver,
                clock = clock,
                parser = parser,
                scope = scope,
                channelLogin = channelLogin,
                preferencesRepository = preferencesRepository
            )
        }
    }

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(
            ConnectionStatus(
                isAlive = false,
                preventSendingMessages = true
            )
        )

    override val connectionStatus = _connectionStatus.asStateFlow()

    override fun start() {
        connect(socketListener = LiveChatThreadListener())
    }

    private inner class LiveChatThreadListener : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            scope.launch {
                val prefs = preferencesRepository.currentPreferences.first()
                with(webSocket) {
                    send("PASS oauth:${prefs.appUser.helixToken}")
                    send("NICK ${prefs.appUser.login}")
                    send("CAP REQ :twitch.tv/tags twitch.tv/commands")
                    send("JOIN $hashChannelName")
                }

                logDebug<LoggedInChatWebSocket> { "Successfully logged in to $hashChannelName" }

                _connectionStatus.update { status ->
                    status.copy(
                        isAlive = true,
                        preventSendingMessages = false
                    )
                }
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            text.lineSequence()
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

            _connectionStatus.update { status ->
                status.copy(
                    isAlive = false,
                    preventSendingMessages = true
                )
            }

            attemptReconnect(listener = this@LiveChatThreadListener)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _connectionStatus.update { status ->
                status.copy(
                    isAlive = false,
                    preventSendingMessages = true
                )
            }
        }
    }

    override fun send(message: CharSequence, inReplyToId: String?) {
        try {
            val inReplyToPrefix = inReplyToId?.let { id -> "@reply-parent-msg-id=$id " } ?: ""
            val privMsg = "${inReplyToPrefix}PRIVMSG $hashChannelName :$message"

            socket?.send(privMsg)

            logDebug<LoggedInChatWebSocket> { "Sent message to $hashChannelName, in reply to $inReplyToId: $message" }
        } catch (e: IOException) {
            logError<LoggedInChatWebSocket>(e) { "Error sending message" }
            emit(
                Command.SendMessageError(
                    throwable = e,
                    timestamp = clock.now()
                )
            )
        }
    }
}
