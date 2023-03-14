package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.Pronoun
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronoun
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronounsApi
import fr.outadoc.justchatting.feature.pronouns.data.UserPronounResponse
import fr.outadoc.justchatting.utils.core.filterValuesNotNull
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class DefaultPronounsRepository(
    private val alejoPronounsApi: AlejoPronounsApi,
) : PronounsRepository {

    private var _pronounCache: Map<String, AlejoPronoun>? = null
    private val _userCache = hashMapOf<Chatter, AlejoPronoun?>()

    private val cacheMutex = Mutex()

    override suspend fun fillPronounsFor(chatters: Set<Chatter>): Map<Chatter, Pronoun> =
        coroutineScope {
            withContext(Dispatchers.IO) {
                cacheMutex.withLock {
                    if (_pronounCache == null) {
                        _pronounCache = alejoPronounsApi
                            .getPronouns()
                            .associateBy { pronoun -> pronoun.id }
                    }
                }

                chatters.map { chatter -> async { chatter to getPronounFor(chatter) } }
                    .awaitAll()
                    .toMap()
                    .filterValuesNotNull()
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
