package fr.outadoc.justchatting.component.twitch.model

data class BttvChannelResponse(
    val channelEmotes: List<BttvEmote>,
    val sharedEmotes: List<BttvEmote>,
) {
    val allEmotes: List<BttvEmote>
        get() = channelEmotes + sharedEmotes
}
