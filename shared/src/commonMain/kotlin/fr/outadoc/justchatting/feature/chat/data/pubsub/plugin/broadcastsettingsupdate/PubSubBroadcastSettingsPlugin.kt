package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.broadcastsettingsupdate

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json
import kotlin.time.Clock

internal class PubSubBroadcastSettingsPlugin(
    private val json: Json,
    private val clock: Clock,
) : PubSubPlugin<PubSubBroadcastSettingsMessage> {
    override fun getTopic(channelId: String): String = "broadcast-settings-update.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> =
        when (val message = json.decodeFromString<PubSubBroadcastSettingsMessage>(payload)) {
            is PubSubBroadcastSettingsMessage.Update -> {
                listOf(
                    ChatEvent.Message.BroadcastSettingsUpdate(
                        timestamp = clock.now(),
                        streamTitle = message.status,
                        categoryId = message.gameId.toString(),
                        categoryName = message.game,
                    ),
                )
            }
        }
}
