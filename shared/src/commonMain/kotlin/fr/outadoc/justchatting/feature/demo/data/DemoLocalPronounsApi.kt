package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.pronouns.domain.LocalPronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronouns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class DemoLocalPronounsApi : LocalPronounsApi {
    private val shePronoun = Pronoun(id = "she", nominative = "she", objective = "her", isSingular = true)
    private val theyPronoun = Pronoun(id = "they", nominative = "they", objective = "them", isSingular = false)

    private val pronounsByUserId =
        mapOf(
            "demo-viewer-lofilistener" to shePronoun,
            "demo-viewer-retrogamerx" to theyPronoun,
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
