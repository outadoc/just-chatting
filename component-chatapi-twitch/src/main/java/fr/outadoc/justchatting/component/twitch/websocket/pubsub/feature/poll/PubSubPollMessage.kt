package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.poll

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class PubSubPollMessage {

    @Serializable
    @SerialName("POLL_CREATE")
    data class Create(
        @SerialName("data")
        val data: Data,
    ) : PubSubPollMessage()

    @Serializable
    @SerialName("POLL_UPDATE")
    data class Update(
        @SerialName("data")
        val data: Data,
    ) : PubSubPollMessage()

    @Serializable
    @SerialName("POLL_COMPLETE")
    data class Complete(
        @SerialName("data")
        val data: Data,
    ) : PubSubPollMessage()

    @Serializable
    @SerialName("POLL_ARCHIVE")
    data class Archive(
        @SerialName("data")
        val data: Data,
    ) : PubSubPollMessage()

    @Serializable
    data class Data(
        @SerialName("poll")
        val poll: Poll,
    ) {
        @Serializable
        data class Poll(
            @SerialName("poll_id")
            val pollId: String,
            @SerialName("status")
            val status: Status,
            @SerialName("title")
            val title: String,
            @SerialName("started_at")
            @Serializable(with = InstantIso8601Serializer::class)
            val startedAt: Instant,
            @SerialName("ended_at")
            @Serializable(with = InstantIso8601Serializer::class)
            val endedAt: Instant?,
            @SerialName("duration_seconds")
            val durationSeconds: Int,
            @SerialName("settings")
            val settings: Settings,
            @SerialName("choices")
            val choices: List<Choice>,
            @SerialName("votes")
            val votes: Votes,
            @SerialName("total_voters")
            val totalVoters: Int,
            @SerialName("remaining_duration_milliseconds")
            val remainingDurationMilliseconds: Int,
            @SerialName("top_contributor")
            val topContributor: String?,
            @SerialName("top_bits_contributor")
            val topBitsContributor: String?,
            @SerialName("top_channel_points_contributor")
            val topChannelPointsContributor: String?,
        ) {
            @Serializable
            enum class Status {

                @SerialName("ACTIVE")
                Active,

                @SerialName("COMPLETED")
                Completed,

                @SerialName("ARCHIVED")
                Archived,
            }

            @Serializable
            data class Settings(
                @SerialName("multi_choice")
                val multiChoice: Setting,
                @SerialName("bits_votes")
                val bitsVotes: Setting,
                @SerialName("channel_points_votes")
                val channelPointsVotes: Setting,
            ) {
                @Serializable
                data class Setting(
                    @SerialName("is_enabled")
                    val isEnabled: Boolean,
                    @SerialName("cost")
                    val cost: Int = 0,
                )
            }

            @Serializable
            data class Choice(
                @SerialName("choice_id")
                val choiceId: String,
                @SerialName("title")
                val title: String,
                @SerialName("votes")
                val votes: Votes,
                @SerialName("total_voters")
                val totalVoters: Int,
            )

            @Serializable
            data class Votes(
                @SerialName("total")
                val total: Int,
                @SerialName("bits")
                val bits: Int,
                @SerialName("channel_points")
                val channelPoints: Int,
                @SerialName("base")
                val base: Int,
            )
        }
    }
}
