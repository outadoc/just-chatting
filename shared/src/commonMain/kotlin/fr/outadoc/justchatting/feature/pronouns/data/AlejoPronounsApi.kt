package fr.outadoc.justchatting.feature.pronouns.data

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Pronoun
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.pronouns.data.model.AlejoPronoun
import fr.outadoc.justchatting.feature.pronouns.data.model.UserPronounResponse
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsApi
import fr.outadoc.justchatting.utils.logging.logError

internal class AlejoPronounsApi(
    private val alejoPronounsClient: AlejoPronounsClient,
    private val preferenceRepository: PreferenceRepository,
) : PronounsApi {

    override suspend fun getPronouns(): Result<List<AlejoPronoun>> {
        return alejoPronounsClient.getPronouns()
    }

    private suspend fun getPronounFor(chatter: Chatter): Pronoun? {
        alejoPronounsClient
            .getPronounsForUser(chatter.login)
            .map { response -> Pronoun(displayPronoun = response) }
    }
}
