package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.poll

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

internal class PubSubPollPlugin(
    private val json: Json,
    private val clock: Clock,
) : PubSubPlugin<PubSubPollMessage> {

    override fun getTopic(channelId: String): String =
        "polls.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> {
        val message = json.decodeFromString<PubSubPollMessage>(payload)
        return listOf(
            ChatEvent.Message.PollUpdate(
                timestamp = clock.now(),
                poll = message.data.poll.map(),
            ),
        )
    }
}
