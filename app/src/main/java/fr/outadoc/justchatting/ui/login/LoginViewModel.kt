package fr.outadoc.justchatting.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.model.AppUser
import fr.outadoc.justchatting.repository.AuthRepository
import fr.outadoc.justchatting.repository.PreferenceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class LoginViewModel(
    private val preferencesRepository: PreferenceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    sealed class State {
        object Initial : State()
        data class LoadWebView(
            val url: HttpUrl,
            val exception: Exception? = null
        ) : State()

        object Done : State()
    }

    private val _state = MutableStateFlow<State>(State.Initial)
    val state: LiveData<State> = _state.asLiveData()

    fun onStart() {
        viewModelScope.launch {
            val prefs = preferencesRepository.currentPreferences.first()

            val user = prefs.appUser
            if (user !is AppUser.NotLoggedIn) {
                preferencesRepository.updatePreferences { current ->
                    current.copy(appUser = AppUser.NotLoggedIn)
                }

                try {
                    val token = user.helixToken
                    if (!token.isNullOrBlank()) {
                        authRepository.revokeToken()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val helixScopes = listOf(
                "chat:read",
                "chat:edit",
                "channel:moderate",
                "channel_editor",
                "whispers:edit",
                "user:read:follows"
            )

            val helixAuthUrl: HttpUrl =
                "https://id.twitch.tv/oauth2/authorize".toHttpUrl()
                    .newBuilder()
                    .addQueryParameter("response_type", "token")
                    .addQueryParameter("client_id", prefs.helixClientId)
                    .addQueryParameter("redirect_uri", prefs.helixRedirect)
                    .addQueryParameter("scope", helixScopes.joinToString(" "))
                    .build()

            _state.update {
                State.LoadWebView(url = helixAuthUrl)
            }
        }
    }

    fun onNavigateToUrl(url: String): Boolean {
        val state = _state.value as? State.LoadWebView ?: return false
        val token = url.toHttpUrlOrNull()?.parseToken() ?: return false

        viewModelScope.launch {
            try {
                preferencesRepository.updatePreferences { prefs ->
                    prefs.copy(
                        appUser = AppUser.NotValidated(
                            helixToken = token
                        )
                    )
                }

                _state.update { State.Done }

            } catch (e: Exception) {
                e.printStackTrace()

                _state.update {
                    state.copy(exception = e)
                }
            }
        }

        return true
    }

    private fun HttpUrl.parseToken(): String? {
        // URL contains query parameters encoded as a path fragment.
        // Copy the path fragment to query parameters and parse them this way.
        return newBuilder()
            .query(fragment)
            .build()
            .queryParameter("access_token")
    }
}
