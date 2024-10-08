package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.prediction

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@OptIn(ExperimentalSerializationApi::class)
internal data class PubSubPredictionMessage(
    @SerialName("data")
    val data: Data,
) {
    @Serializable
    data class Data(
        @SerialName("timestamp")
        val timestampIso: String,
        @SerialName("event")
        val event: Event,
    )

    @Serializable
    @JsonClassDiscriminator("status")
    sealed class Event {

        @Serializable
        @SerialName("ACTIVE")
        data class Active(
            @SerialName("id")
            val id: String,
            @SerialName("channel_id")
            val channelId: String,
            @SerialName("title")
            val title: String,
            @SerialName("created_at")
            val createdAtIso: String,
            @SerialName("outcomes")
            val outcomes: List<Outcome>,
            @SerialName("prediction_window_seconds")
            val predictionWindowSeconds: Int,
            @SerialName("winning_outcome_id")
            val winningOutcomeId: String?,
        ) : Event()

        @Serializable
        @SerialName("LOCKED")
        data class Locked(
            @SerialName("id")
            val id: String,
            @SerialName("channel_id")
            val channelId: String,
            @SerialName("title")
            val title: String,
            @SerialName("created_at")
            val createdAtIso: String,
            @SerialName("locked_at")
            val lockedAtIso: String?,
            @SerialName("outcomes")
            val outcomes: List<Outcome>,
            @SerialName("prediction_window_seconds")
            val predictionWindowSeconds: Int,
        ) : Event()

        @Serializable
        @SerialName("RESOLVE_PENDING")
        data class ResolvePending(
            @SerialName("id")
            val id: String,
            @SerialName("channel_id")
            val channelId: String,
            @SerialName("created_at")
            val createdAtIso: String,
            @SerialName("ended_at")
            val endedAtIso: String?,
            @SerialName("locked_at")
            val lockedAtIso: String?,
            @SerialName("outcomes")
            val outcomes: List<Outcome>,
            @SerialName("prediction_window_seconds")
            val predictionWindowSeconds: Int,
            @SerialName("title")
            val title: String,
            @SerialName("winning_outcome_id")
            val winningOutcomeId: String,
        ) : Event()

        @Serializable
        @SerialName("RESOLVED")
        data class Resolved(
            @SerialName("id")
            val id: String,
            @SerialName("channel_id")
            val channelId: String,
            @SerialName("title")
            val title: String,
            @SerialName("created_at")
            val createdAtIso: String,
            @SerialName("ended_at")
            val endedAtIso: String?,
            @SerialName("locked_at")
            val lockedAtIso: String?,
            @SerialName("outcomes")
            val outcomes: List<Outcome>,
            @SerialName("prediction_window_seconds")
            val predictionWindowSeconds: Int,
            @SerialName("winning_outcome_id")
            val winningOutcomeId: String,
        ) : Event()
    }

    @Serializable
    data class Outcome(
        @SerialName("id")
        val id: String,
        @SerialName("title")
        val title: String,
        @SerialName("color")
        val color: String,
        @SerialName("total_points")
        val totalPoints: Int,
        @SerialName("total_users")
        val totalUsers: Int,
        @SerialName("badge")
        val badge: Badge,
        @SerialName("top_predictors")
        val topPredictors: List<Predictor>,
    ) {
        @Serializable
        enum class Type {
            @SerialName("WIN")
            Win,

            @SerialName("LOSE")
            Lose,
        }

        @Serializable
        data class Badge(
            @SerialName("version")
            val version: String,
            @SerialName("set_id")
            val setId: String,
        )
    }

    @Serializable
    data class Predictor(
        @SerialName("id")
        val id: String,
        @SerialName("event_id")
        val eventId: String,
        @SerialName("outcome_id")
        val outcomeId: String,
        @SerialName("channel_id")
        val channelId: String,
        @SerialName("points")
        val points: Int,
        @SerialName("predicted_at")
        val predictedAtIso: String,
        @SerialName("updated_at")
        val updatedAtIso: String,
        @SerialName("user_id")
        val userId: String,
        @SerialName("user_display_name")
        val userDisplayName: String,
        @SerialName("result")
        val result: Result?,
    ) {
        @Serializable
        data class Result(
            @SerialName("type")
            val type: Outcome.Type,
            @SerialName("points_won")
            val pointsWon: Int?,
        )
    }
}
