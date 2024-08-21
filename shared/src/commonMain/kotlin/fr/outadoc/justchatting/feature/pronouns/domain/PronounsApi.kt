package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun

internal interface PronounsApi {
    suspend fun getPronouns(): Result<List<Pronoun>>
}
