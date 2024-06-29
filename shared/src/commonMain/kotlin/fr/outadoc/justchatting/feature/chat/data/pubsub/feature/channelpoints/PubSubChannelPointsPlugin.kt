package fr.outadoc.justchatting.feature.chat.data.pubsub.feature.channelpoints

import dev.icerock.moko.resources.format
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Icon
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import fr.outadoc.justchatting.shared.MR
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

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
                    ChatEvent.Message.Highlighted(
                        timestamp = message.data.redemption.redeemedAt ?: clock.now(),
                        metadata = ChatEvent.Message.Highlighted.Metadata(
                            title = MR.plurals.user_redeemed
                                .format(
                                    number = message.data.redemption.reward.cost,
                                    message.data.redemption.user.displayName,
                                    message.data.redemption.reward.title,
                                    message.data.redemption.reward.cost,
                                ),
                            titleIcon = Icon.Toll,
                            subtitle = null,
                        ),
                        body = null,
                    ),
                )
            }
        }
}
