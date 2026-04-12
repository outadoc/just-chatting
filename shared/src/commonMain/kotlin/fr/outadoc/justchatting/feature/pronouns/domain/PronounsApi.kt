package fr.outadoc.justchatting.feature.pronouns.domain

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds

internal interface PronounsApi {
    public suspend fun getPronouns(): Result<List<Pronoun>>

    public suspend fun getUserPronouns(chatter: Chatter): Result<UserPronounIds>
}
