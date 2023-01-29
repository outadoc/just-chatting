package fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.feature.channelpoints

import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import fr.outadoc.justchatting.feature.chat.data.model.PointReward
import fr.outadoc.justchatting.feature.chat.data.websocket.eventsub.plugin.EventSubPlugin
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class EventSubChannelPointsPlugin(
    private val clock: Clock,
    private val json: Json,
) : EventSubPlugin<EventSubRewardMessage> {

    override fun getTopic(channelId: String): String =
        "community-points-channel-v1.$channelId"

    override fun parseMessage(message: String): ChatCommand =
        when (val res = json.decodeFromString<EventSubRewardMessage>(message)) {
            is EventSubRewardMessage.Redeemed -> {
                PointReward(
                    id = res.data.redemption.id,
                    userId = res.data.redemption.user.id,
                    userLogin = res.data.redemption.user.login,
                    userName = res.data.redemption.user.displayName,
                    message = res.data.redemption.userAddedMessage,
                    rewardTitle = res.data.redemption.reward.title,
                    rewardCost = res.data.redemption.reward.cost,
                    timestamp = res.data.redemption.redeemedAt ?: clock.now(),
                    rewardImage = null,
                )
            }
        }
}
