package fr.outadoc.justchatting.component.twitch.model

data class EmoteSetResponse(
    val template: String,
    val data: List<TwitchEmote>
)
