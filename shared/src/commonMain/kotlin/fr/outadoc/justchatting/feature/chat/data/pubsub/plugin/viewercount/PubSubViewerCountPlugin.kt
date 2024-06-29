package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.viewercount

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.pubsub.PubSubPlugin
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

internal class PubSubViewerCountPlugin(
    private val json: Json,
    private val clock: Clock,
) : PubSubPlugin<PubSubViewerCountMessage> {

    override fun getTopic(channelId: String): String =
        "video-playback-by-id.$channelId"

    override fun parseMessage(payload: String): List<ChatEvent> =
        when (val message = json.decodeFromString<PubSubViewerCountMessage>(payload)) {
            is PubSubViewerCountMessage.ViewCount -> {
                listOf(
                    ChatEvent.Message.ViewerCountUpdate(
                        timestamp = clock.now(),
                        viewerCount = message.viewers,
                    ),
                )
            }
        }
}
