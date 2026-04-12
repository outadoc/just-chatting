package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronouns
import kotlinx.coroutines.flow.Flow

internal interface LocalPronounsApi {
    public suspend fun arePronounsSynced(): Boolean

    public suspend fun saveAndReplacePronouns(pronouns: List<Pronoun>)

    public suspend fun getPronounsForUser(userId: String): Flow<UserPronouns?>

    public suspend fun saveUserPronouns(userPronoun: UserPronounIds)
}
