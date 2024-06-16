package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.pinnedmessage

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json

class PubSubPinnedMessagePlugin(
    private val json: Json,
) : PubSubPlugin<PubSubPinnedMessage> {

    override fun getTopic(channelId: String): String =
        "pinned-chat-updates-v1.$channelId"

    override suspend fun parseMessage(payload: String): List<ChatEvent> =
        when (val message = json.decodeFromString<PubSubPinnedMessage>(payload)) {
            is PubSubPinnedMessage.Pin -> {
                listOf(
                    ChatEvent.PinnedMessageUpdate(
                        pinnedMessage = message.map(),
                    ),
                )
            }

            is PubSubPinnedMessage.Update -> emptyList()

            is PubSubPinnedMessage.Unpin -> listOf(
                ChatEvent.PinnedMessageUpdate(
                    pinnedMessage = null,
                ),
            )
        }
}
