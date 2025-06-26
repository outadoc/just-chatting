package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.channelpoints

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.Redemption
import fr.outadoc.justchatting.feature.chat.domain.model.Reward
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

internal class PubSubChannelPointsPlugin(
    private val clock: Clock,
    private val json: Json,
) : PubSubPlugin<PubSubRewardMessage> {

    override fun getTopic(channelId: String): String =
        "community-points-channel-v1.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> =
        when (val message = json.decodeFromString<PubSubRewardMessage>(payload)) {
            is PubSubRewardMessage.Redeemed -> {
                listOf(
                    ChatEvent.Message.RedemptionUpdate(
                        timestamp = message.data.redemption.redeemedAtIso
                            ?.let { Instant.parse(it) }
                            ?: clock.now(),
                        redemption = Redemption(
                            id = message.data.redemption.id,
                            userId = message.data.redemption.user.id,
                            userLogin = message.data.redemption.user.login,
                            userDisplayName = message.data.redemption.user.displayName,
                            userAddedMessage = message.data.redemption.userAddedMessage,
                            redeemedAt = message.data.redemption.redeemedAtIso
                                ?.let { Instant.parse(it) },
                            reward = Reward(
                                id = message.data.redemption.reward.id,
                                title = message.data.redemption.reward.title,
                                cost = message.data.redemption.reward.cost,
                            ),
                        ),
                    ),
                )
            }
        }
}
