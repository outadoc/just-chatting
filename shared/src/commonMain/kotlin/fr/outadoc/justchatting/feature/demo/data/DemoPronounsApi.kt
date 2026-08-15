package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds

/**
 * Never actually reached in demo mode: [DemoLocalPronounsApi.arePronounsSynced] always returns
 * `true`, so [fr.outadoc.justchatting.feature.pronouns.domain.PronounsRepository] never calls this.
 * Implemented anyway to satisfy the interface.
 */
internal class DemoPronounsApi : PronounsApi {
    override suspend fun getPronouns(): Result<List<Pronoun>> = Result.success(emptyList())

    override suspend fun getUserPronouns(chatter: Chatter): Result<UserPronounIds> =
        Result.success(UserPronounIds(userId = chatter.id, mainPronounId = null, altPronounId = null))
}
