package fr.outadoc.justchatting.feature.chat.data.pubsub.feature.viewercount

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.serialization.json.Json

internal class PubSubViewerCountPlugin(
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
