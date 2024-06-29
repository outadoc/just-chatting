package fr.outadoc.justchatting.feature.pronouns.data

import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.Pronoun
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.pronouns.data.model.AlejoPronoun
import fr.outadoc.justchatting.feature.pronouns.data.model.UserPronounResponse
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsApi
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class AlejoPronounsApi(
    private val alejoPronounsClient: AlejoPronounsClient,
    private val preferenceRepository: PreferenceRepository,
) : PronounsApi {

    private var pronounCache: Map<String, AlejoPronoun>? = null
    private val userCache = hashMapOf<Chatter, AlejoPronoun?>()

    private val cacheMutex = Mutex()

    override suspend fun fillPronounsFor(chatters: Set<Chatter>): Map<Chatter, Pronoun?> =
        coroutineScope {
            if (!preferenceRepository.currentPreferences.first().enablePronouns) {
                return@coroutineScope emptyMap()
            }

            withContext(DispatchersProvider.io) {
                cacheMutex.withLock {
                    if (pronounCache == null) {
                        alejoPronounsClient
                            .getPronouns()
                            .fold(
                                onSuccess = { pronouns ->
                                    pronounCache = pronouns.associateBy { pronoun -> pronoun.id }
                                },
                                onFailure = { e ->
                                    logError<AlejoPronounsApi>(e) { "Error while fetching pronouns from Alejo API" }
                                },
                            )
                    }
                }

                chatters
                    .map { chatter -> async { chatter to getPronounFor(chatter) } }
                    .awaitAll()
                    .toMap()
            }
        }

    private suspend fun getPronounFor(chatter: Chatter): Pronoun? {
        val pronounCache = pronounCache ?: return null

        val cached: AlejoPronoun? =
            userCache.getOrPut(chatter) {
                val userPronouns: UserPronounResponse? =
                    alejoPronounsClient
                        .getPronounsForUser(chatter.login)
                        .onFailure { exception ->
                            logError<AlejoPronounsApi>(exception) { "Error while getting pronoun for $chatter" }
                        }
                        .getOrNull()
                        ?.firstOrNull()

                pronounCache[userPronouns?.pronounId]
            }

        return cached?.display?.let { displayPronoun ->
            Pronoun(displayPronoun = displayPronoun)
        }
    }
}
