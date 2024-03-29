package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.Pronoun
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronoun
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronounsApi
import fr.outadoc.justchatting.feature.pronouns.data.UserPronounResponse
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DefaultPronounsRepository(
    private val alejoPronounsApi: AlejoPronounsApi,
    private val preferenceRepository: PreferenceRepository,
) : PronounsRepository {

    private var _pronounCache: Map<String, AlejoPronoun>? = null
    private val _userCache = hashMapOf<Chatter, AlejoPronoun?>()

    private val cacheMutex = Mutex()

    override suspend fun fillPronounsFor(chatters: Set<Chatter>): Map<Chatter, Pronoun?> =
        coroutineScope {
            if (!preferenceRepository.currentPreferences.first().enablePronouns) {
                return@coroutineScope emptyMap()
            }

            withContext(DispatchersProvider.io) {
                cacheMutex.withLock {
                    if (_pronounCache == null) {
                        try {
                            _pronounCache = alejoPronounsApi
                                .getPronouns()
                                .associateBy { pronoun -> pronoun.id }
                        } catch (e: Exception) {
                            logError<DefaultPronounsRepository>(e) { "Error while fetching pronouns from Alejo API" }
                        }
                    }
                }

                chatters.map { chatter -> async { chatter to getPronounFor(chatter) } }
                    .awaitAll()
                    .toMap()
            }
        }

    private suspend fun getPronounFor(chatter: Chatter): Pronoun? {
        val pronounCache = _pronounCache ?: return null

        val cached: AlejoPronoun? =
            _userCache.getOrPut(chatter) {
                try {
                    val userPronouns: UserPronounResponse? =
                        alejoPronounsApi
                            .getPronounsForUser(chatter.login)
                            .firstOrNull()

                    pronounCache[userPronouns?.pronounId]
                } catch (e: Exception) {
                    logError<DefaultPronounsRepository>(e) { "Error while getting pronoun for $chatter" }
                    null
                }
            }

        return cached?.display?.let { displayPronoun ->
            Pronoun(displayPronoun = displayPronoun)
        }
    }
}
