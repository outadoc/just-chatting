package fr.outadoc.justchatting.feature.chat.data.irc

import fr.outadoc.justchatting.feature.chat.domain.handler.ChatEventHandler
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ConnectionStatus
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import fr.outadoc.justchatting.utils.core.delayWithJitter
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import fr.outadoc.justchatting.utils.logging.logInfo
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

/**
 * Logged in chat thread.
 *
 * Needed because user's own messages are only send when logged out. This thread handles
 * user-specific NOTICE and USERSTATE messages, and [LoggedInChatWebSocket] handles the rest.
 *
 * Use this class to write messages to the chat.
 */
internal class LoggedInChatWebSocket(
    private val networkStateObserver: NetworkStateObserver,
    private val parser: TwitchIrcCommandParser,
    private val httpClient: HttpClient,
) : ChatEventHandler {
    companion object {
        private const val ENDPOINT = "wss://irc-ws.chat.twitch.tv"
    }

    private val _connectionStatus: MutableStateFlow<ConnectionStatus> =
        MutableStateFlow(
            ConnectionStatus(
                isAlive = false,
                registeredListeners = 0,
            ),
        )

    override val connectionStatus = _connectionStatus.asStateFlow()

    override fun getEventFlow(
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> = channelFlow {
        _connectionStatus.update { it.copy(registeredListeners = 1) }
        try {
            networkStateObserver.state.collectLatest { netState ->
                if (netState is NetworkStateObserver.NetworkState.Available) {
                    logDebug<LoggedInChatWebSocket> { "Network is available, listening" }
                    while (currentCoroutineContext().isActive) {
                        _connectionStatus.update { it.copy(isAlive = true) }
                        try {
                            listen(channelLogin, appUser)
                        } catch (e: Exception) {
                            logError<LoggedInChatWebSocket>(e) { "Socket was closed" }
                        }
                        _connectionStatus.update { it.copy(isAlive = false) }
                        delayWithJitter(1.seconds, maxJitter = 3.seconds)
                    }
                } else {
                    logDebug<LoggedInChatWebSocket> { "Network is out, waiting" }
                    _connectionStatus.update { it.copy(isAlive = false) }
                }
            }
        } finally {
            _connectionStatus.update { it.copy(registeredListeners = 0) }
        }
    }.flowOn(DispatchersProvider.io)

    private suspend fun ProducerScope<ChatEvent>.listen(
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ) {
        httpClient.webSocket(ENDPOINT) {
            logDebug<LoggedInChatWebSocket> { "Socket open, logging in" }

            send("PASS oauth:${appUser.token}")
            send("NICK ${appUser.userLogin}")
            send("CAP REQ :twitch.tv/tags twitch.tv/commands")
            send("JOIN #$channelLogin")

            // Receive messages
            while (isActive) {
                when (val received = incoming.receive()) {
                    is Frame.Text -> {
                        received
                            .readText()
                            .lines()
                            .filter { it.isNotBlank() }
                            .forEach { line ->
                                handleMessage(line) { event ->
                                    this@listen.send(event)
                                }
                            }
                    }

                    else -> {}
                }
            }
        }
    }

    private suspend fun DefaultWebSocketSession.handleMessage(
        received: String,
        emit: suspend (ChatEvent) -> Unit,
    ) {
        logInfo<LoggedInChatWebSocket> { "received: $received" }

        when (val command = parser.parse(received)) {
            is ChatEvent.Message.Notice -> {
                emit(command)
            }

            is ChatEvent.Command.UserState -> {
                emit(command)
            }

            is ChatEvent.Command.Ping -> {
                send("PONG :tmi.twitch.tv")
            }

            else -> {}
        }
    }
}
