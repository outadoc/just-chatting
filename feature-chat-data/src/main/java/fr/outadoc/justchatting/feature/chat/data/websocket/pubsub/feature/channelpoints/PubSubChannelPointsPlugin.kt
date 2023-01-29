package fr.outadoc.justchatting.feature.chat.data.websocket.pubsub.feature.channelpoints

import fr.outadoc.justchatting.feature.chat.data.model.ChatCommand
import fr.outadoc.justchatting.feature.chat.data.model.PointReward
import fr.outadoc.justchatting.feature.chat.data.websocket.pubsub.feature.channelpoints.model.PubSubRewardMessage
import fr.outadoc.justchatting.feature.chat.data.websocket.pubsub.plugin.PubSubPlugin
import kotlinx.datetime.Clock
import kotlin.reflect.KClass

class PubSubChannelPointsPlugin(
    private val clock: Clock,
) : PubSubPlugin<PubSubRewardMessage> {

    override val model: KClass<PubSubRewardMessage> = PubSubRewardMessage::class

    override fun getTopic(channelId: String): String =
        "community-points-channel-v1.$channelId"

    override fun parseMessage(message: PubSubRewardMessage): ChatCommand {
        return when (message) {
            is PubSubRewardMessage.Redeemed -> {
                PointReward(
                    id = message.data.redemption.id,
                    userId = message.data.redemption.user.id,
                    userLogin = message.data.redemption.user.login,
                    userName = message.data.redemption.user.displayName,
                    message = message.data.redemption.userAddedMessage,
                    rewardTitle = message.data.redemption.reward.title,
                    rewardCost = message.data.redemption.reward.cost,
                    timestamp = message.data.redemption.redeemedAt ?: clock.now(),
                    rewardImage = null,
                )
            }
        }
    }
}
