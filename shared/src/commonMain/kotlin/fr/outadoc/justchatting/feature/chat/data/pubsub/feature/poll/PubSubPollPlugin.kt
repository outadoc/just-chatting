package fr.outadoc.justchatting.feature.chat.data.pubsub.feature.poll

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json

internal class PubSubPollPlugin(
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
