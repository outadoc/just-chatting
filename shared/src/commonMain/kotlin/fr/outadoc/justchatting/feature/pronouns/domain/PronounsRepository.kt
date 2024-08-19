package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Pronoun
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronounsApi
import fr.outadoc.justchatting.feature.pronouns.data.db.LocalPronounsApi
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class PronounsRepository(
    private val pronounsApi: PronounsApi,
    private val localPronounsApi: LocalPronounsApi,
    private val preferenceRepository: PreferenceRepository,
) {
    private val cacheMutex = Mutex()

    suspend fun fillPronounsFor(chatters: Set<Chatter>): Map<Chatter, Pronoun?> {
        return coroutineScope {
            if (!preferenceRepository.currentPreferences.first().enablePronouns) {
                return@coroutineScope emptyMap()
            }

            withContext(DispatchersProvider.io) {
                cacheMutex.withLock {
                    if (!localPronounsApi.arePronounsSynced()) {
                        pronounsApi
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
        pronounsApi.fillPronounsFor(chatters)
    }
}
