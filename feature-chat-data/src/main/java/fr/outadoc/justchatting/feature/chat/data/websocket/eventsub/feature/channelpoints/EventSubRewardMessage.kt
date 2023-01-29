package fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.feature.channelpoints

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventSubRewardMessage(
    @SerialName("id")
    val id: String,
    @SerialName("broadcaster_user_id")
    val broadcasterUserId: String,
    @SerialName("broadcaster_user_login")
    val broadcasterUserLogin: String,
    @SerialName("broadcaster_user_name")
    val broadcasterUserName: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("user_login")
    val userLogin: String,
    @SerialName("user_name")
    val userName: String,
    @SerialName("status")
    val status: String,
    @SerialName("redeemed_at")
    @Serializable(with = InstantIso8601Serializer::class)
    val redeemedAt: Instant,
    @SerialName("user_input")
    val userAddedMessage: String,
    @SerialName("reward")
    val reward: Reward
) {
    @Serializable
    data class Reward(
        @SerialName("id")
        val id: String,
        @SerialName("title")
        val title: String,
        @SerialName("cost")
        val cost: Int,
        @SerialName("prompt")
        val prompt: String
    )
}
