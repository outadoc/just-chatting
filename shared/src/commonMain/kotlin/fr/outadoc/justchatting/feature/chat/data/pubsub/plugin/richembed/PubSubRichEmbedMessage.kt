package fr.outadoc.justchatting.feature.chat.data.pubsub.plugin.richembed

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class PubSubRichEmbedMessage {

    @Serializable
    @SerialName("chat_rich_embed")
    data class RichEmbed(
        @SerialName("data")
        val data: Data,
    ) : PubSubRichEmbedMessage() {

        @Serializable
        data class Data(
            @SerialName("message_id")
            val messageId: String,
            @SerialName("request_url")
            val requestUrl: String,
            @SerialName("author_name")
            val authorName: String,
            @SerialName("thumbnail_url")
            val thumbnailUrl: String,
            @SerialName("title")
            val title: String,
            @SerialName("twitch_metadata")
            val metadata: Metadata,
        )

        @Serializable
        data class Metadata(
            @Serializable
            @SerialName("clip_metadata")
            val clipMetadata: Clip? = null,
        ) {
            @Serializable
            data class Clip(
                @SerialName("id")
                val id: String,
                @SerialName("slug")
                val slug: String,
                @SerialName("game")
                val game: String,
                @SerialName("channel_display_name")
                val channelDisplayName: String,
                @SerialName("broadcaster_id")
                val broadcasterId: String,
                @SerialName("curator_id")
                val curatorId: String,
            )
        }
    }
}
