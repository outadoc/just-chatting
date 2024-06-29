package fr.outadoc.justchatting.feature.chat.data.irc.recent

import fr.outadoc.justchatting.feature.chat.data.irc.TwitchIrcCommandParser
import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.withContext

internal class RecentMessagesRepository(
    private val recentMessagesApi: RecentMessagesApi,
    private val parser: TwitchIrcCommandParser,
) {
    suspend fun loadRecentMessages(channelLogin: String, limit: Int): Result<List<ChatEvent>> =
        withContext(DispatchersProvider.io) {
            recentMessagesApi
                .getRecentMessages(channelLogin, limit)
                .map { response ->
                    response.messages
                        .filterNot { message -> message.isBlank() }
                        .mapNotNull { message -> parser.parse(message) }
                }
        }
}
