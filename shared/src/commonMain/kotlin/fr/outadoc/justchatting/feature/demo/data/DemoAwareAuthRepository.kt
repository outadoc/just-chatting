package fr.outadoc.justchatting.feature.demo.data

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.demo.domain.DemoModeRepository
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.DefaultAuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
internal class DemoAwareAuthRepository(
    private val demoModeRepository: DemoModeRepository,
    private val real: Lazy<DefaultAuthRepository>,
    private val demo: DemoAuthRepository,
) : AuthRepository {
    override val currentUser: Flow<AppUser> =
        demoModeRepository.isDemoMode.flatMapLatest { isDemoMode ->
            if (isDemoMode) demo.currentUser else real.value.currentUser
        }

    override suspend fun saveToken(token: String) {
        current().saveToken(token)
    }

    override suspend fun logout() {
        current().logout()
    }

    override fun getExternalAuthorizeUrl(): Uri = current().getExternalAuthorizeUrl()

    private fun current(): AuthRepository =
        if (demoModeRepository.isDemoMode.value) demo else real.value
}
