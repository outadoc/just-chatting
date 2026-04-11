package fr.outadoc.justchatting.feature.pronouns.domain.model

import androidx.compose.runtime.Immutable

@Immutable
public data class UserPronouns(
    val userId: String,
    val mainPronoun: Pronoun?,
    val altPronoun: Pronoun?,
)
