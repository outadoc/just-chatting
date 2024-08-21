package fr.outadoc.justchatting.feature.pronouns.data.db

import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import kotlinx.coroutines.flow.Flow

internal interface LocalPronounsApi {
    suspend fun arePronounsSynced(): Boolean
    suspend fun getPronounForUser(userId: String): Flow<Pronoun?>
    suspend fun saveUserPronoun(userId: String, pronoun: Pronoun)
}
