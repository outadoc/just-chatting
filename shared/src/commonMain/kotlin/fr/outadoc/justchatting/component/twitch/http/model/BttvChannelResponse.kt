package fr.outadoc.justchatting.component.twitch.http.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class BttvChannelResponse(
    @SerialName("channelEmotes")
    val channelEmotes: List<BttvEmote>,
    @SerialName("sharedEmotes")
    val sharedEmotes: List<BttvEmote>,
) {
    val allEmotes: List<BttvEmote>
        get() = channelEmotes + sharedEmotes
}
