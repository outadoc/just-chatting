package fr.outadoc.justchatting.feature.demo.data

import fr.outadoc.justchatting.feature.demo.domain.DemoModeRepository
import fr.outadoc.justchatting.feature.pronouns.data.LocalPronounsDb
import fr.outadoc.justchatting.feature.pronouns.domain.LocalPronounsApi
import fr.outadoc.justchatting.feature.pronouns.domain.model.Pronoun
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronounIds
import fr.outadoc.justchatting.feature.pronouns.domain.model.UserPronouns
import kotlinx.coroutines.flow.Flow

internal class DemoAwareLocalPronounsApi(
    private val demoModeRepository: DemoModeRepository,
    private val real: Lazy<LocalPronounsDb>,
    private val demo: DemoLocalPronounsApi,
) : LocalPronounsApi {
    private fun current(): LocalPronounsApi = if (demoModeRepository.isDemoMode.value) demo else real.value

    override suspend fun arePronounsSynced(): Boolean = current().arePronounsSynced()

    override suspend fun saveAndReplacePronouns(pronouns: List<Pronoun>) {
        current().saveAndReplacePronouns(pronouns)
    }

    override suspend fun getPronounsForUser(userId: String): Flow<UserPronouns?> = current().getPronounsForUser(userId)

    override suspend fun saveUserPronouns(userPronoun: UserPronounIds) {
        current().saveUserPronouns(userPronoun)
    }
}
