package fr.outadoc.justchatting.feature.chat.data.irc

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlin.time.Duration

/**
 * Logged in chat thread.
 *
 * Needed because user's own messages are only sent when logged out. This thread handles
 * user-specific NOTICE and USERSTATE messages, and [LiveChatWebSocket] handles the rest.
 *
 * Use this class to write messages to the chat.
 */
internal class LoggedInChatWebSocket(
    networkStateObserver: NetworkStateObserver,
    parser: TwitchIrcCommandParser,
    httpClient: HttpClient,
    dispatchersProvider: DispatchersProvider,
    endpoint: String = DEFAULT_ENDPOINT,
    messageTimeout: Duration = DEFAULT_MESSAGE_TIMEOUT,
) : BaseChatWebSocket(
        networkStateObserver = networkStateObserver,
        parser = parser,
        httpClient = httpClient,
        dispatchersProvider = dispatchersProvider,
        endpoint = endpoint,
        messageTimeout = messageTimeout,
        // Only NOTICE/USERSTATE are read below; skip fully parsing every other command
        // (in particular PRIVMSG, by far the highest-volume one) just to discard it.
        commandsOfInterest = setOf("NOTICE", "USERSTATE"),
    ) {
    override val logTag: String = "LoggedInChatWebSocket"

    override fun getEventFlow(
        channelId: String,
        channelLogin: String,
        appUser: AppUser.LoggedIn,
    ): Flow<ChatEvent> =
        chatEventFlow {
            Session(
                onConnected = {
                    sendCommand("PASS oauth:${appUser.token}")
                    sendCommand("NICK ${appUser.userLogin}")
                    sendCommand("CAP REQ :twitch.tv/tags twitch.tv/commands")
                    sendCommand("JOIN #$channelLogin")
                },
                onCommandReceived = { command ->
                    when (command) {
                        is ChatEvent.Message.Notice,
                        is ChatEvent.Command.UserState,
                        -> {
                            emit(command)
                        }

                        else -> {}
                    }
                },
            )
        }
}
