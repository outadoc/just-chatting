package fr.outadoc.justchatting.feature.chat.data.recent

import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import fr.outadoc.justchatting.feature.chat.data.parser.ChatMessageParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecentMessagesRepository(
    private val recentMessagesApi: RecentMessagesApi,
    private val parser: ChatMessageParser
) {
    suspend fun loadRecentMessages(channelLogin: String, limit: Int): List<ChatCommand> =
        withContext(Dispatchers.IO) {
            recentMessagesApi.getRecentMessages(channelLogin, limit)
                .messages
                .filterNot { message -> message.isBlank() }
                .mapNotNull { message -> parser.parse(message) }
        }
}
