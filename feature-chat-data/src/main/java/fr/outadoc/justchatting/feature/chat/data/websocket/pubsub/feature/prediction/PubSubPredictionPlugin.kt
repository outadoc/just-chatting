package fr.outadoc.justchatting.feature.chat.data.websocket.pubsub.feature.prediction

import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import fr.outadoc.justchatting.feature.chat.data.model.Command
import fr.outadoc.justchatting.feature.chat.data.websocket.pubsub.plugin.PubSubPlugin
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

    override fun parseMessage(message: String): ChatCommand? {
        val res = json.decodeFromString<PubSubPredictionMessage>(message)
        return when (val event = res.data.event) {
            is PubSubPredictionMessage.Event.Active -> {
                Command.Notice(
                    message = """
                        Prediction in progress!
                        ${event.title}
                        ${
                        event.outcomes.joinToString(separator = "\n") { outcome ->
                            "${outcome.title}: ${outcome.totalPoints.formatNumber()}"
                        }
                    }
                    """.trimIndent(),
                    timestamp = clock.now(),
                    messageId = null
                )
            }

            is PubSubPredictionMessage.Event.Locked -> {
                Command.Notice(
                    message = """
                        Prediction locked!
                        ${event.title}
                        ${
                        event.outcomes.joinToString(separator = "\n") { outcome ->
                            "${outcome.title}: ${outcome.totalPoints.formatNumber()}"
                        }
                    }
                    """.trimIndent(),
                    timestamp = clock.now(),
                    messageId = null
                )
            }

            is PubSubPredictionMessage.Event.ResolvePending -> null
            is PubSubPredictionMessage.Event.Resolved -> {
                val winner: PubSubPredictionMessage.Outcome =
                    event.outcomes.first { outcome -> outcome.id == event.winningOutcomeId }

                Command.Notice(
                    message = """
                        Prediction ended!
                        ${event.title}
                        Winner: ${winner.title}
                        ${
                        event.outcomes.joinToString(separator = "\n") { outcome ->
                            "${outcome.title}: ${outcome.totalPoints.formatNumber()}"
                        }
                    }
                    """.trimIndent(),
                    timestamp = clock.now(),
                    messageId = null
                )
            }
        }
    }
}
