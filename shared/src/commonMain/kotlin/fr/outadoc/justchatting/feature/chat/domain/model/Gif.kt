package fr.outadoc.justchatting.feature.chat.domain.model

import androidx.compose.runtime.Immutable

@Immutable
public data class Gif(
    val id: String,
    val url: String,
    val description: String,
)
