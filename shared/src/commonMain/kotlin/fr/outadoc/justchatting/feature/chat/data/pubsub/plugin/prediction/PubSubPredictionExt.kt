package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.prediction

import fr.outadoc.justchatting.feature.chat.domain.model.Badge
import fr.outadoc.justchatting.feature.chat.domain.model.Prediction
import kotlin.time.Duration.Companion.seconds

internal fun PubSubPredictionMessage.Outcome.map(): Prediction.Outcome {
    return Prediction.Outcome(
        id = id,
        title = title,
        color = when (color) {
            "PINK" -> "#e0008e"
            "BLUE" -> "#1e69ff"
            else -> color
        },
        totalPoints = totalPoints,
        totalUsers = totalUsers,
        badge = Badge(
            id = badge.setId,
            version = badge.version,
        ),
    )
}

internal fun PubSubPredictionMessage.map(): Prediction {
    return when (val prediction = data.event) {
        is PubSubPredictionMessage.Event.Active -> {
            Prediction(
                id = prediction.id,
                title = prediction.title,
                status = Prediction.Status.Active,
                createdAt = prediction.createdAt,
                endedAt = null,
                lockedAt = null,
                outcomes = prediction.outcomes.map { outcome -> outcome.map() },
                predictionWindow = prediction.predictionWindowSeconds.seconds,
                winningOutcome = null,
            )
        }

        is PubSubPredictionMessage.Event.Locked -> {
            Prediction(
                id = prediction.id,
                title = prediction.title,
                status = Prediction.Status.Locked,
                createdAt = prediction.createdAt,
                endedAt = null,
                lockedAt = prediction.lockedAt,
                outcomes = prediction.outcomes.map { outcome -> outcome.map() },
                predictionWindow = prediction.predictionWindowSeconds.seconds,
                winningOutcome = null,
            )
        }

        is PubSubPredictionMessage.Event.ResolvePending -> {
            val outcomes = prediction.outcomes.map { outcome -> outcome.map() }
            Prediction(
                id = prediction.id,
                title = prediction.title,
                status = Prediction.Status.ResolvePending,
                createdAt = prediction.createdAt,
                endedAt = prediction.endedAt,
                lockedAt = prediction.lockedAt,
                outcomes = prediction.outcomes.map { outcome -> outcome.map() },
                predictionWindow = prediction.predictionWindowSeconds.seconds,
                winningOutcome = outcomes.first { outcome -> outcome.id == prediction.winningOutcomeId },
            )
        }

        is PubSubPredictionMessage.Event.Resolved -> {
            val outcomes = prediction.outcomes.map { outcome -> outcome.map() }
            Prediction(
                id = prediction.id,
                title = prediction.title,
                status = Prediction.Status.Resolved,
                createdAt = prediction.createdAt,
                endedAt = prediction.endedAt,
                lockedAt = prediction.lockedAt,
                outcomes = prediction.outcomes.map { outcome -> outcome.map() },
                predictionWindow = prediction.predictionWindowSeconds.seconds,
                winningOutcome = outcomes.first { outcome -> outcome.id == prediction.winningOutcomeId },
            )
        }
    }
}
