package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.channelpoints

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Icon
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.user_redeemed
import fr.outadoc.justchatting.utils.core.desc
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.getPluralString

class PubSubChannelPointsPlugin(
    private val clock: Clock,
    private val json: Json,
) : PubSubPlugin<PubSubRewardMessage> {

    override fun getTopic(channelId: String): String =
        "community-points-channel-v1.$channelId"

    override suspend fun parseMessage(payload: String): List<ChatEvent> =
        when (val message = json.decodeFromString<PubSubRewardMessage>(payload)) {
            is PubSubRewardMessage.Redeemed -> {
                listOf(
                    ChatEvent.Message.Highlighted(
                        timestamp = message.data.redemption.redeemedAt ?: clock.now(),
                        metadata = ChatEvent.Message.Highlighted.Metadata(
                            title = getPluralString(
                                Res.plurals.user_redeemed,
                                quantity = message.data.redemption.reward.cost,
                                message.data.redemption.user.displayName,
                                message.data.redemption.reward.title,
                                message.data.redemption.reward.cost,
                            ).desc(),
                            titleIcon = Icon.Toll,
                            subtitle = null,
                        ),
                        body = null,
                    ),
                )
            }
        }
}
