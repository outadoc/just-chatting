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

    override fun parseMessage(payload: String): List<ChatEvent> {
        val message = json.decodeFromString<PubSubPredictionMessage>(payload)
        val event = when (val prediction = message.data.event) {
            is PubSubPredictionMessage.Event.Active -> {
                ChatEvent.Message.Highlighted(
                    header = buildString {
                        appendLine("Prediction in progress!")
                        appendLine(prediction.title)
                        prediction.outcomes.forEach { outcome ->
                            appendLine(
                                "${outcome.title}: ${outcome.totalPoints.formatNumber()}",
                            )
                        }
                    }.trimEnd(),
                    body = null,
                    timestamp = clock.now(),
                )
            }

            is PubSubPredictionMessage.Event.Locked -> {
                ChatEvent.Message.Highlighted(
                    header = buildString {
                        appendLine("Prediction locked!")
                        appendLine(prediction.title)
                        prediction.outcomes.forEach { outcome ->
                            appendLine(
                                "${outcome.title}: ${outcome.totalPoints.formatNumber()}",
                            )
                        }
                    }.trimEnd(),
                    body = null,
                    timestamp = clock.now(),
                )
            }

            is PubSubPredictionMessage.Event.ResolvePending -> null
            is PubSubPredictionMessage.Event.Resolved -> {
                val winner: PubSubPredictionMessage.Outcome =
                    prediction.outcomes.first { outcome -> outcome.id == prediction.winningOutcomeId }

                ChatEvent.Message.Highlighted(
                    header = buildString {
                        appendLine("Prediction ended!")
                        appendLine(prediction.title)
                        appendLine("Winner: ${winner.title}")
                        prediction.outcomes.forEach { outcome ->
                            appendLine(
                                "${outcome.title}: ${outcome.totalPoints.formatNumber()}",
                            )
                        }
                    }.trimEnd(),
                    body = null,
                    timestamp = clock.now(),
                )
            }
        }

        return listOfNotNull(event)
    }
}
