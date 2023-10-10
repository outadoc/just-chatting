package fr.outadoc.justchatting.component.twitch.websocket.irc.recent

import fr.outadoc.justchatting.component.twitch.websocket.irc.TwitchIrcCommandParser
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.IrcEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecentMessagesRepository(
    private val recentMessagesApi: RecentMessagesApi,
    private val parser: TwitchIrcCommandParser,
) {
    suspend fun loadRecentMessages(channelLogin: String, limit: Int): List<IrcEvent> =
        withContext(Dispatchers.IO) {
            recentMessagesApi.getRecentMessages(channelLogin, limit)
                .messages
                .filterNot { message -> message.isBlank() }
                .mapNotNull { message -> parser.parse(message) }
        }
}
