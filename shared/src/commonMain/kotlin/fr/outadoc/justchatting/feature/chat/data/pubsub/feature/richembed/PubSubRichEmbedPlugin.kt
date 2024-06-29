package fr.outadoc.justchatting.feature.chat.data.pubsub.feature.richembed

import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json

internal class PubSubRichEmbedPlugin(
    private val json: Json,
) : PubSubPlugin<PubSubRichEmbedMessage> {

    override fun getTopic(channelId: String): String =
        "stream-chat-room-v1.$channelId"

    override fun parseMessage(payload: String): List<ChatListItem> {
        val message = json.decodeFromString<PubSubRichEmbedMessage>(payload)
        return listOfNotNull(
            when (message) {
                is PubSubRichEmbedMessage.RichEmbed -> message.data.map()
            },
        )
    }
}
