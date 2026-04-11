package fr.outadoc.justchatting.feature.pronouns.domain.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class UserPronounIds(
    val userId: String,
    val mainPronounId: String?,
    val altPronounId: String?,
)
