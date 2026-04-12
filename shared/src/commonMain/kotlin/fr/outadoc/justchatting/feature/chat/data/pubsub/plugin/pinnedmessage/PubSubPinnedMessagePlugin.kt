package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.pinnedmessage

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json
import kotlin.time.Clock

internal class PubSubPinnedMessagePlugin(
    private val json: Json,
    private val clock: Clock,
) : PubSubPlugin<PubSubPinnedMessage> {
    override fun getTopic(channelId: String): String = "pinned-chat-updates-v1.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> =
        when (val message = json.decodeFromString<PubSubPinnedMessage>(payload)) {
            is PubSubPinnedMessage.Pin -> {
                listOf(
                    ChatEvent.Message.PinnedMessageUpdate(
                        timestamp = message.data.message.startsAt,
                        pinnedMessage = message.map(),
                    ),
                )
            }

            is PubSubPinnedMessage.Update -> {
                emptyList()
            }

            is PubSubPinnedMessage.Unpin -> {
                listOf(
                    ChatEvent.Message.PinnedMessageUpdate(
                        timestamp = clock.now(),
                        pinnedMessage = null,
                    ),
                )
            }
        }
}
