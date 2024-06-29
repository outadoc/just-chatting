package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Pronoun

internal class PronounsRepository(
    private val pronounsApi: PronounsApi,
) {
    suspend fun fillPronounsFor(chatters: Set<Chatter>): Map<Chatter, Pronoun?> {
        return pronounsApi.fillPronounsFor(chatters)
    }
}
