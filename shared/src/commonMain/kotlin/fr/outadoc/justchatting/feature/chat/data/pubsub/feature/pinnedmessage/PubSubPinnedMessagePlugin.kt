package fr.outadoc.justchatting.feature.chat.data.pubsub.feature.pinnedmessage

import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json

internal class PubSubPinnedMessagePlugin(
    private val json: Json,
) : PubSubPlugin<PubSubPinnedMessage> {

    override fun getTopic(channelId: String): String =
        "pinned-chat-updates-v1.$channelId"

    override fun parseMessage(payload: String): List<ChatListItem> =
        when (val message = json.decodeFromString<PubSubPinnedMessage>(payload)) {
            is PubSubPinnedMessage.Pin -> {
                listOf(
                    ChatListItem.PinnedMessageUpdate(
                        pinnedMessage = message.map(),
                    ),
                )
            }

            is PubSubPinnedMessage.Update -> emptyList()

            is PubSubPinnedMessage.Unpin -> listOf(
                ChatListItem.PinnedMessageUpdate(
                    pinnedMessage = null,
                ),
            )
        }
}
