package fr.outadoc.justchatting.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import fr.outadoc.justchatting.model.AppUser
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class ChannelChatViewModel(
    private val repository: TwitchService,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    sealed class State {
        object Loading : State()
        data class Loaded(
            val appUser: AppUser,
            val loadedUser: User?,
            val stream: Stream?
        ) : State()
    }

    private val _channelLogin = MutableSharedFlow<String>(replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: LiveData<State.Loaded> = _channelLogin
        .map { channelLogin ->
            try {
                val user = loadUser(channelLogin) ?: error("User not loaded")
                val stream = repository.loadStreamWithUser(channelId = user.id)
                stream to user
            } catch (e: Exception) {
                e.printStackTrace()
                null to null
            }
        }
        .flatMapLatest { (stream, loadedUser) ->
            userPreferencesRepository.appUser.map { user ->
                State.Loaded(
                    appUser = user,
                    loadedUser = loadedUser,
                    stream = stream
                )
            }
        }
        .onStart { State.Loading }
        .asLiveData()

    private suspend fun loadUser(channelLogin: String): User? {
        return try {
            repository.loadUsersByLogin(logins = listOf(channelLogin))?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadStream(channelLogin: String) {
        _channelLogin.tryEmit(channelLogin)
    }
}
