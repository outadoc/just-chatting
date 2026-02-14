package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.channelpoints

import fr.outadoc.justchatting.feature.chat.data.http.Reward
import fr.outadoc.justchatting.feature.shared.data.model.User
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
            val timestampIso: String? = null,
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
            val redeemedAtIso: String? = null,
            @SerialName("reward")
            val reward: Reward,
        )
    }
}
