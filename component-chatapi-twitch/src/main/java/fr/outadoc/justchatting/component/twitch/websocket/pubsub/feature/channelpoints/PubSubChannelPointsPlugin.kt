package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.channelpoints

import android.content.Context
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import fr.outadoc.justchatting.component.twitch.R
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PubSubChannelPointsPlugin(
    private val clock: Clock,
    private val json: Json,
    private val context: Context,
) : PubSubPlugin<PubSubRewardMessage> {

    override fun getTopic(channelId: String): String =
        "community-points-channel-v1.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> =
        when (val message = json.decodeFromString<PubSubRewardMessage>(payload)) {
            is PubSubRewardMessage.Redeemed -> {
                listOf(
                    ChatEvent.Message.Highlighted(
                        title = context.resources.getQuantityString(
                            R.plurals.user_redeemed,
                            message.data.redemption.reward.cost,
                            message.data.redemption.user.displayName,
                            message.data.redemption.reward.title,
                            message.data.redemption.reward.cost,
                        ),
                        titleIconResId = R.drawable.ic_toll,
                        body = null,
                        timestamp = message.data.redemption.redeemedAt ?: clock.now(),
                    ),
                )
            }
        }
}
