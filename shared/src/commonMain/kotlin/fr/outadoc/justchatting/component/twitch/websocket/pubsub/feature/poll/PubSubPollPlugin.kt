package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.poll

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json

class PubSubPollPlugin(
    private val json: Json,
) : PubSubPlugin<PubSubPollMessage> {

    override fun getTopic(channelId: String): String =
        "polls.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> {
        val message = json.decodeFromString<PubSubPollMessage>(payload)
        return listOf(
            ChatEvent.PollUpdate(poll = message.data.poll.map()),
        )
    }
}
