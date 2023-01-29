package fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.feature.channelpoints

import fr.outadoc.justchatting.component.twitch.model.Reward
import fr.outadoc.justchatting.component.twitch.model.User
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class EventSubRewardMessage {
    @Serializable
    @SerialName("reward-redeemed")
    data class Redeemed(
        @SerialName("data")
        val data: Data,
    ) : EventSubRewardMessage() {

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
            val userAddedMessage: String,
            @SerialName("redeemed_at")
            @Serializable(with = InstantIso8601Serializer::class)
            val redeemedAt: Instant? = null,
            @SerialName("reward")
            val reward: Reward,
        )
    }
}
