package fr.outadoc.justchatting.feature.pronouns.data

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Pronoun
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsApi

internal class AlejoPronounsApi(
    private val alejoPronounsClient: AlejoPronounsClient,
    private val preferenceRepository: PreferenceRepository,
) : PronounsApi {

    override suspend fun getPronouns(): Result<List<Pronoun>> {
        TODO()
    }

    private suspend fun getPronounFor(chatter: Chatter): Pronoun? {
        TODO()
    }
}
