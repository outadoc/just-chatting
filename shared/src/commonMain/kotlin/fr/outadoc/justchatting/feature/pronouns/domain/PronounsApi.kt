package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.data.model.AlejoPronoun

internal interface PronounsApi {
    suspend fun fillPronounsFor(chatters: Set<Chatter>): Map<Chatter, Pronoun?>
    suspend fun getPronouns(): Result<List<AlejoPronoun>>
}
