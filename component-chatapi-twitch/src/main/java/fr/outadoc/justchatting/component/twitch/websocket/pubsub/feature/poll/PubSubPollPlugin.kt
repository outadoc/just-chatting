package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.poll

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.core.formatPercent
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PubSubPollPlugin(
    private val clock: Clock,
    private val json: Json,
) : PubSubPlugin<PubSubPollMessage> {

    override fun getTopic(channelId: String): String =
        "polls.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> {
        val event = when (val message = json.decodeFromString<PubSubPollMessage>(payload)) {
            is PubSubPollMessage.Create -> {
                message.data.poll.toChatEvent("A poll is starting!")
            }

            is PubSubPollMessage.Update -> {
                message.data.poll.toChatEvent("A poll is in progress…")
            }

            is PubSubPollMessage.Complete -> {
                message.data.poll.toChatEvent("A poll has ended!")
            }

            is PubSubPollMessage.Archive -> {
                null
            }
        }

        return listOfNotNull(event)
    }

    private fun PubSubPollMessage.Data.Poll.toChatEvent(header: String): ChatEvent.Message {
        return ChatEvent.Message.Highlighted(
            header = buildString {
                appendLine(header)
                appendLine(title)
                choices.forEach { choice ->
                    val ratio: Float =
                        choice.votes.total.toFloat() / votes.total.toFloat()
                    appendLine(
                        "${choice.title}: ${ratio.formatPercent()} (${choice.votes.total.formatNumber()} votes)",
                    )
                }
            }.trimEnd(),
            body = null,
            timestamp = clock.now(),
        )
    }
}
