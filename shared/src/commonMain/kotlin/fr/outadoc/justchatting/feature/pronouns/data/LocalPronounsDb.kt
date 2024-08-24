package fr.outadoc.justchatting.feature.pronouns.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import fr.outadoc.justchatting.data.db.PronounQueries
import fr.outadoc.justchatting.feature.pronouns.domain.LocalPronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronouns
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

internal class LocalPronounsDb(
    private val db: PronounQueries,
    private val clock: Clock,
) : LocalPronounsApi {

    private val pronouns: Flow<PersistentMap<String, Pronoun>> =
        db.getPronouns()
            .asFlow()
            .mapToList(DispatchersProvider.io)
            .map { result ->
                result
                    .associateBy { it.id }
                    .mapValues { (_, result) ->
                        Pronoun(
                            id = result.id,
                            nominative = result.nominative,
                            objective = result.objective,
                            isSingular = result.singular > 0,
                        )
                    }
                    .toPersistentMap()
            }

    override suspend fun arePronounsSynced(): Boolean {
        return !pronouns.firstOrNull().isNullOrEmpty()
    }

    override suspend fun saveAndReplacePronouns(pronouns: List<Pronoun>) {
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

    override suspend fun saveUserPronouns(userPronoun: UserPronounIds) {
        withContext(DispatchersProvider.io) {
            db.saveUserPronoun(
                user_id = userPronoun.userId,
                pronoun_id = userPronoun.mainPronounId,
                alt_pronoun_id = userPronoun.altPronounId,
                updated_at = clock.now().toEpochMilliseconds(),
            )
        }
    }

    override suspend fun getPronounsForUser(userId: String): Flow<UserPronouns?> {
        return withContext(DispatchersProvider.io) {
            db.getUserPronoun(userId)
                .asFlow()
                .mapToOneOrNull(DispatchersProvider.io)
                .combine(pronouns) { result, pronouns ->
                    when {
                        result == null -> {
                            null
                        }

                        Instant.fromEpochMilliseconds(result.updated_at) < clock.now() - MaxPronounCacheLife -> {
                            null
                        }

                        else -> {
                            UserPronouns(
                                userId = result.user_id,
                                mainPronoun = pronouns[result.pronoun_id],
                                altPronoun = pronouns[result.alt_pronoun_id],
                            )
                        }
                    }
                }
        }
    }

    private companion object {
        private val MaxPronounCacheLife = 30.days
    }
}
