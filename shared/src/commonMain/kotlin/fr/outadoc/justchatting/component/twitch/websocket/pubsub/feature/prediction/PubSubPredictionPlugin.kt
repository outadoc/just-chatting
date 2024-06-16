package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.prediction

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json

class PubSubPredictionPlugin(
    private val json: Json,
) : PubSubPlugin<PubSubPredictionMessage> {

    override fun getTopic(channelId: String): String =
        "predictions-channel-v1.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> {
        val message = json.decodeFromString<PubSubPredictionMessage>(payload)
        return listOfNotNull(
            ChatEvent.PredictionUpdate(
                message.map(),
            ),
        )
    }
}
