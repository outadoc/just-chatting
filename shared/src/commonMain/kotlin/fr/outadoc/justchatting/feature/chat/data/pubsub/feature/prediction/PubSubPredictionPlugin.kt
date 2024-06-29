package fr.outadoc.justchatting.feature.chat.data.pubsub.feature.prediction

import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json

internal class PubSubPredictionPlugin(
    private val json: Json,
) : PubSubPlugin<PubSubPredictionMessage> {

    override fun getTopic(channelId: String): String =
        "predictions-channel-v1.$channelId"

    override fun parseMessage(payload: String): List<ChatListItem> {
        val message = json.decodeFromString<PubSubPredictionMessage>(payload)
        return listOfNotNull(
            ChatListItem.PredictionUpdate(
                message.map(),
            ),
        )
    }
}
