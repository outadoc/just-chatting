package fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.feature.channelpoints

import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import fr.outadoc.justchatting.feature.chat.data.model.PointReward
import fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.client.model.EventSubMessageWithEvent
import fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.plugin.EventSubPlugin
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class EventSubChannelPointsPlugin(
    private val json: Json,
) : EventSubPlugin<EventSubRewardMessage> {

    override val subscriptionType: String = "channel.channel_points_custom_reward_redemption.add"

    override fun parseMessage(message: String): ChatCommand {
        val res = json.decodeFromString<EventSubMessageWithEvent<EventSubRewardMessage>>(message)
        val event = res.payload.event
        return PointReward(
            id = event.id,
            userId = event.userId,
            userLogin = event.userLogin,
            userName = event.userName,
            message = event.userAddedMessage,
            rewardTitle = event.reward.title,
            rewardCost = event.reward.cost,
            timestamp = event.redeemedAt,
            rewardImage = null,
        )
    }
}
