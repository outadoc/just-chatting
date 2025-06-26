package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.richembed

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json
import kotlin.time.Clock

internal class PubSubRichEmbedPlugin(
    private val json: Json,
    private val clock: Clock,
) : PubSubPlugin<PubSubRichEmbedMessage> {

    override fun getTopic(channelId: String): String =
        "stream-chat-room-v1.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> {
        val message = json.decodeFromString<PubSubRichEmbedMessage>(payload)
        return listOfNotNull(
            when (message) {
                is PubSubRichEmbedMessage.RichEmbed -> {
                    ChatEvent.Message.RichEmbed(
                        timestamp = clock.now(),
                        messageId = message.data.messageId,
                        title = message.data.title,
                        requestUrl = message.data.requestUrl,
                        thumbnailUrl = message.data.thumbnailUrl,
                        authorName = message.data.authorName,
                        channelName = message.data.metadata.clipMetadata?.channelDisplayName,
                    )
                }
            },
        )
    }
}
