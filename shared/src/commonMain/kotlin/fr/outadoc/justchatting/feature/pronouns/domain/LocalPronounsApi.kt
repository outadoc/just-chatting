package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronouns
import kotlinx.coroutines.flow.Flow

internal interface LocalPronounsApi {
    suspend fun arePronounsSynced(): Boolean

    suspend fun saveAndReplacePronouns(pronouns: List<Pronoun>)

    suspend fun getPronounsForUser(userId: String): Flow<UserPronouns?>

    suspend fun saveUserPronouns(userPronoun: UserPronounIds)
}
