package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.channelpoints

import fr.outadoc.justchatting.component.twitch.http.model.Reward
import fr.outadoc.justchatting.component.twitch.http.model.User
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class PubSubRewardMessage {
    @Serializable
    @SerialName("reward-redeemed")
    data class Redeemed(
        @SerialName("data")
        val data: Data,
    ) : PubSubRewardMessage() {

        @Serializable
        data class Data(
            @SerialName("timestamp")
            @Serializable(with = InstantIso8601Serializer::class)
            val timestamp: Instant? = null,
            @SerialName("redemption")
            val redemption: Redemption,
        )

        @Serializable
        data class Redemption(
            @SerialName("id")
            val id: String,
            @SerialName("user")
            val user: User,
            @SerialName("user_input")
            val userAddedMessage: String? = null,
            @SerialName("redeemed_at")
            @Serializable(with = InstantIso8601Serializer::class)
            val redeemedAt: Instant? = null,
            @SerialName("reward")
            val reward: Reward,
        )
    }
}
