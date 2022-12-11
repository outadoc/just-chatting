package fr.outadoc.justchatting.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.component.preferences.AppPreferences
import fr.outadoc.justchatting.component.preferences.AppUser
import fr.outadoc.justchatting.component.preferences.PreferenceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferenceRepository: PreferenceRepository,
    private val authRepository: fr.outadoc.justchatting.component.twitch.domain.repository.AuthRepository
) : ViewModel() {

    val appPreferences: StateFlow<AppPreferences> =
        preferenceRepository.currentPreferences.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AppPreferences()
        )

    fun updatePreferences(appPreferences: AppPreferences) {
        viewModelScope.launch {
            preferenceRepository.updatePreferences { appPreferences }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val currentUser = preferenceRepository.currentPreferences.first().appUser
            preferenceRepository.updatePreferences { current ->
                current.copy(appUser = AppUser.NotLoggedIn)
            }

            try {
                val token = currentUser.helixToken
                if (!token.isNullOrBlank()) {
                    authRepository.revokeToken()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
