package fr.outadoc.justchatting.component.twitch.websocket.pubsub.feature.viewercount

import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json

class PubSubViewerCountPlugin(
    private val json: Json,
) : PubSubPlugin<PubSubViewerCountMessage> {

    override fun getTopic(channelId: String): String =
        "video-playback-by-id.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> =
        when (val message = json.decodeFromString<PubSubViewerCountMessage>(payload)) {
            is PubSubViewerCountMessage.ViewCount -> {
                listOf(
                    ChatEvent.ViewerCountUpdate(
                        viewerCount = message.viewers,
                    ),
                )
            }
        }
}
