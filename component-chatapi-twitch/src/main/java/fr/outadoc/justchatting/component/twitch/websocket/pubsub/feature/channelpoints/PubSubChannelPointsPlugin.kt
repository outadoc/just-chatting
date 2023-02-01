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

    override fun parseMessage(message: String): List<ChatEvent> =
        when (val res = json.decodeFromString<PubSubRewardMessage>(message)) {
            is PubSubRewardMessage.Redeemed -> {
                listOf(
                    ChatEvent.Highlighted(
                        header = context.resources.getQuantityString(
                            R.plurals.user_redeemed,
                            res.data.redemption.reward.cost,
                            res.data.redemption.user.displayName,
                            res.data.redemption.reward.title,
                            res.data.redemption.reward.cost,
                        ),
                        headerIconResId = R.drawable.ic_toll,
                        data = null,
                        timestamp = res.data.redemption.redeemedAt ?: clock.now(),
                    ),
                )
            }
        }
}
