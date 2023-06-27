package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.component.chatapi.common.Pronoun

interface PronounsRepository {
    suspend fun fillPronounsFor(chatters: Set<Chatter>): Map<Chatter, Pronoun?>
}
