package fr.outadoc.justchatting.feature.chat.data.pubsub.feature.viewercount

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed class PubSubViewerCountMessage {

    @Serializable
    @SerialName("viewcount")
    data class ViewCount(
        @SerialName("viewers")
        val viewers: Int,
    ) : PubSubViewerCountMessage()
}
