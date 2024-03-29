package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.richembed

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PubSubRichEmbedPlugin(
    private val json: Json,
) : PubSubPlugin<PubSubRichEmbedMessage> {

    override fun getTopic(channelId: String): String =
        "stream-chat-room-v1.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> {
        val message = json.decodeFromString<PubSubRichEmbedMessage>(payload)
        return listOfNotNull(
            when (message) {
                is PubSubRichEmbedMessage.RichEmbed -> message.data.map()
            },
        )
    }
}
