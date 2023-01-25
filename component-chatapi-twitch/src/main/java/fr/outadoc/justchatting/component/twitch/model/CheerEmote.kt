package fr.outadoc.justchatting.component.twitch.model

data class CheerEmote(
    val prefix: String,
    val tiers: List<CheerEmoteTier>
)
