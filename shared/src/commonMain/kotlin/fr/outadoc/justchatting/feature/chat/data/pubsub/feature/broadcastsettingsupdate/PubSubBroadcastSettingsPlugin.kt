package fr.outadoc.justchatting.feature.chat.data.pubsub.feature.broadcastsettingsupdate

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json

internal class PubSubBroadcastSettingsPlugin(
    private val json: Json,
) : PubSubPlugin<PubSubBroadcastSettingsMessage> {

    override fun getTopic(channelId: String): String =
        "broadcast-settings-update.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> =
        when (val message = json.decodeFromString<PubSubBroadcastSettingsMessage>(payload)) {
            is PubSubBroadcastSettingsMessage.Update -> {
                listOf(
                    ChatListItem.BroadcastSettingsUpdate(
                        streamTitle = message.status,
                        gameName = message.game,
                    ),
                )
            }
        }
}
