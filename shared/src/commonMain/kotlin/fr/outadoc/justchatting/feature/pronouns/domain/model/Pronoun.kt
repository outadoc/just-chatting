package fr.outadoc.justchatting.feature.pronouns.domain.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class Pronoun(
    val id: String,
    val nominative: String,
    val objective: String,
    val isSingular: Boolean,
) {
    val displayPronoun: String = arrayOf(nominative, objective).joinToString("/")
}
