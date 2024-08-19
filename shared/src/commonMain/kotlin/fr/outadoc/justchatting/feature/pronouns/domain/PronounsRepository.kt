package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Pronoun
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.pronouns.data.db.LocalPronounsApi
import kotlinx.coroutines.sync.Mutex

internal class PronounsRepository(
    private val pronounsApi: PronounsApi,
    private val localPronounsApi: LocalPronounsApi,
    private val preferenceRepository: PreferenceRepository,
) {
    private val cacheMutex = Mutex()

    suspend fun fillPronounsFor(chatters: Set<Chatter>): Map<Chatter, Pronoun?> {
        TODO()
    }
}
