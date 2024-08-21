package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds

internal interface PronounsApi {
    suspend fun getPronouns(): Result<List<Pronoun>>
    suspend fun getUserPronouns(userId: String): Result<UserPronounIds>
}
