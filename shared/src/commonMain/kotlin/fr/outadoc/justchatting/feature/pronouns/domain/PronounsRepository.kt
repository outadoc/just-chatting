package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class PronounsRepository(
    private val pronounsApi: PronounsApi,
    private val localPronounsApi: LocalPronounsApi,
    private val preferenceRepository: PreferenceRepository,
) {
    private val cacheMutex = Mutex()

    suspend fun fillPronounsFor(chatters: Set<Chatter>): Map<Chatter, Pronoun?> =
        withContext(DispatchersProvider.io) {
            if (!preferenceRepository.currentPreferences.first().enablePronouns) {
                return@withContext emptyMap()
            }

            cacheMutex.withLock {
                if (!localPronounsApi.arePronounsSynced()) {
                    pronounsApi
                        .getPronouns()
                        .fold(
                            onSuccess = { pronouns ->
                                localPronounsApi.saveAndReplacePronouns(pronouns)
                            },
                            onFailure = { e ->
                                logError<PronounsRepository>(e) { "Error while fetching pronouns from Alejo API" }
                            },
                        )
                }
            }

            chatters
                .map { chatter ->
                    chatter to localPronounsApi.getPronounsForUser(chatter.id).firstOrNull()
                }
                .map { (chatter, userPronoun) ->
                    async {
                        if (userPronoun != null) {
                            chatter to userPronoun
                        } else {
                            chatter to pronounsApi
                                .getUserPronouns(chatter)
                                .fold(
                                    onSuccess = { userPronoun ->
                                        localPronounsApi
                                            .saveUserPronouns(userPronoun)

                                        localPronounsApi
                                            .getPronounsForUser(chatter.id)
                                            .firstOrNull()
                                    },
                                    onFailure = { e ->
                                        logError<PronounsRepository>(e) { "Error while fetching pronouns for user ${chatter.id}" }
                                        null
                                    },
                                )
                        }
                    }
                }
                .awaitAll()
                .toMap()
                .mapValues { (_, userPronoun) ->
                    userPronoun?.mainPronoun
                }
        }
}
