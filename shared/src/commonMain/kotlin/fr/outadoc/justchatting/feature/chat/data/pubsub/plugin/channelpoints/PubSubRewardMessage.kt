package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.channelpoints

import fr.outadoc.justchatting.feature.home.data.model.Reward
import fr.outadoc.justchatting.feature.home.data.model.User
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class PubSubRewardMessage {
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
