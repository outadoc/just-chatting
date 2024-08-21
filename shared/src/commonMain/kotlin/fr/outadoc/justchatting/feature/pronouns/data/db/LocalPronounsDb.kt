package fr.outadoc.justchatting.feature.pronouns.data.db

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import fr.outadoc.justchatting.data.db.PronounQueries
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class LocalPronounsDb(
    private val db: PronounQueries,
) : LocalPronounsApi {

    override suspend fun arePronounsSynced(): Boolean {
        return withContext(DispatchersProvider.io) {
            db.getPronouns().executeAsList().isNotEmpty()
        }
    }

    suspend fun saveAndReplacePronouns(pronouns: List<Pronoun>) {
        withContext(DispatchersProvider.io) {
            db.transaction {
                db.clearPronouns()
                pronouns.forEach { pronoun ->
                    db.savePronoun(
                        id = pronoun.id,
                        nominative = pronoun.nominative,
                        objective = pronoun.objective,
                        singular = if (pronoun.isSingular) 1 else 0,
                    )
                }
            }
        }
    }

    override suspend fun saveUserPronoun(userId: String, pronoun: Pronoun) {
        withContext(DispatchersProvider.io) {
            db.saveUserPronoun(
                user_id = userId,
                pronoun_id = pronoun.id,
            )
        }
    }

    override suspend fun getPronounForUser(userId: String): Flow<Pronoun?> {
        return withContext(DispatchersProvider.io) {
            db.getUserPronoun(userId)
                .asFlow()
                .mapToOne(DispatchersProvider.io)
                .map { result ->
                    Pronoun(
                        id = result.id,
                        nominative = result.nominative,
                        objective = result.objective,
                        isSingular = result.singular > 0,
                    )
                }
        }
    }
}
