package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.pronouns.domain.LocalPronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronouns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class DemoLocalPronounsApi : LocalPronounsApi {
    private val shePronoun = Pronoun(id = "she", nominative = "she", objective = "her", isSingular = true)
    private val hePronoun = Pronoun(id = "he", nominative = "he", objective = "him", isSingular = true)

    private val pronounsByUserId =
        mapOf(
            "avens" to hePronoun,
            "demo-solanum" to shePronoun,
        )

    override suspend fun arePronounsSynced(): Boolean = true

    override suspend fun saveAndReplacePronouns(pronouns: List<Pronoun>) {
        // No-op: demo pronouns are always considered synced.
    }

    override suspend fun getPronounsForUser(userId: String): Flow<UserPronouns?> =
        flowOf(
            pronounsByUserId[userId]?.let { pronoun ->
                UserPronouns(userId = userId, mainPronoun = pronoun, altPronoun = null)
            },
        )

    override suspend fun saveUserPronouns(userPronoun: UserPronounIds) {
        // No-op: never called, since arePronounsSynced() is always true.
    }
}
