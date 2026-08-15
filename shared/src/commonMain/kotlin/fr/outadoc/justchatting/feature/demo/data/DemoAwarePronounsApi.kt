package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.demo.domain.DemoModeRepository
import fr.outadoc.justchatting.feature.pronouns.data.AlejoPronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.PronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds

internal class DemoAwarePronounsApi(
    private val demoModeRepository: DemoModeRepository,
    private val real: Lazy<AlejoPronounsApi>,
    private val demo: DemoPronounsApi,
) : PronounsApi {
    private fun current(): PronounsApi = if (demoModeRepository.isDemoMode.value) demo else real.value

    override suspend fun getPronouns(): Result<List<Pronoun>> = current().getPronouns()

    override suspend fun getUserPronouns(chatter: Chatter): Result<UserPronounIds> = current().getUserPronouns(chatter)
}
