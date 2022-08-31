package fr.outadoc.justchatting.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.repository.ChatPreferencesRepository
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class ChannelChatViewModel(
    private val repository: TwitchService,
    chatPreferencesRepository: ChatPreferencesRepository,
    userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    sealed class State {
        object Loading : State()
        data class Loaded(
            val user: fr.outadoc.justchatting.model.User,
            val loadedUser: User?,
            val stream: Stream?,
            val showTimestamps: Boolean = false,
            val animateEmotes: Boolean = true
        ) : State()
    }

    private val _channelId = MutableSharedFlow<String>(replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: LiveData<State.Loaded> = _channelId
        .map { channelId ->
            try {
                val stream = repository.loadStreamWithUser(channelId = channelId)
                val user = stream?.channelUser ?: loadUser(channelId)
                stream to user
            } catch (e: Exception) {
                e.printStackTrace()
                null to null
            }
        }
        .flatMapLatest { (stream, loadedUser) ->
            combine(
                userPreferencesRepository.user,
                chatPreferencesRepository.showTimestamps,
                chatPreferencesRepository.animateEmotes
            ) { user, showTimestamps, animateEmotes ->
                State.Loaded(
                    user = user,
                    loadedUser = loadedUser,
                    stream = stream,
                    showTimestamps = showTimestamps,
                    animateEmotes = animateEmotes
                )
            }
        }
        .onStart { State.Loading }
        .asLiveData()

    private suspend fun loadUser(
        channelId: String
    ): User? {
        return try {
            repository.loadUsersById(ids = listOf(channelId))?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadStream(channelId: String) {
        _channelId.tryEmit(channelId)
    }
}
