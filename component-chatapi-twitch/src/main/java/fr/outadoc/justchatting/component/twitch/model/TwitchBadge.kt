package fr.outadoc.justchatting.component.twitch.model

import kotlinx.collections.immutable.ImmutableMap

data class TwitchBadge(
    val id: String,
    val version: String,
    val urls: ImmutableMap<Float, String>,
    val title: String? = null
)
