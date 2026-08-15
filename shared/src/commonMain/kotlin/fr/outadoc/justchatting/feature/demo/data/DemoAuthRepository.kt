package fr.outadoc.justchatting.feature.demo.data

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.demo.domain.DemoModeRepository
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class DemoAuthRepository(
    private val demoModeRepository: DemoModeRepository,
) : AuthRepository {
    override val currentUser: Flow<AppUser> =
        demoModeRepository.isDemoMode.map { isDemoMode ->
            if (isDemoMode) {
                AppUser.LoggedIn(
                    userId = DemoData.CURRENT_USER_ID,
                    userLogin = DemoData.CURRENT_USER_LOGIN,
                    token = DemoData.CURRENT_USER_TOKEN,
                )
            } else {
                AppUser.NotLoggedIn
            }
        }

    override suspend fun saveToken(token: String) {
        // No-op: demo mode has no real token to persist.
    }

    override suspend fun logout() {
        demoModeRepository.setDemoMode(false)
    }

    override fun getExternalAuthorizeUrl(): Uri {
        error("Demo mode has no external authorize URL")
    }
}
