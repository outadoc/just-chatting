package fr.outadoc.justchatting.feature.timeline.domain.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class StreamCategory(
    val id: String,
    val name: String,
)
