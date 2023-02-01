package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.prediction

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import fr.outadoc.justchatting.utils.core.formatNumber
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PubSubPredictionPlugin(
    private val clock: Clock,
    private val json: Json,
) : PubSubPlugin<PubSubPredictionMessage> {

    override fun getTopic(channelId: String): String =
        "predictions-channel-v1.$channelId"

    override fun parseMessage(message: String): List<ChatEvent> {
        val res = json.decodeFromString<PubSubPredictionMessage>(message)
        val event = when (val event = res.data.event) {
            is PubSubPredictionMessage.Event.Active -> {
                ChatEvent.Highlighted(
                    header = buildString {
                        appendLine("Prediction in progress!")
                        appendLine(event.title)
                        event.outcomes.forEach { outcome ->
                            appendLine(
                                "${outcome.title}: ${outcome.totalPoints.formatNumber()}",
                            )
                        }
                    }.trimEnd(),
                    data = null,
                    timestamp = clock.now(),
                )
            }

            is PubSubPredictionMessage.Event.Locked -> {
                ChatEvent.Highlighted(
                    header = buildString {
                        appendLine("Prediction locked!")
                        appendLine(event.title)
                        event.outcomes.forEach { outcome ->
                            appendLine(
                                "${outcome.title}: ${outcome.totalPoints.formatNumber()}",
                            )
                        }
                    }.trimEnd(),
                    data = null,
                    timestamp = clock.now(),
                )
            }

            is PubSubPredictionMessage.Event.ResolvePending -> null
            is PubSubPredictionMessage.Event.Resolved -> {
                val winner: PubSubPredictionMessage.Outcome =
                    event.outcomes.first { outcome -> outcome.id == event.winningOutcomeId }

                ChatEvent.Highlighted(
                    header = buildString {
                        appendLine("Prediction ended!")
                        appendLine(event.title)
                        appendLine("Winner: ${winner.title}")
                        event.outcomes.forEach { outcome ->
                            appendLine(
                                "${outcome.title}: ${outcome.totalPoints.formatNumber()}",
                            )
                        }
                    }.trimEnd(),
                    data = null,
                    timestamp = clock.now(),
                )
            }
        }

        return listOfNotNull(event)
    }
}
