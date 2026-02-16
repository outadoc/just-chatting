package fr.outadoc.justchatting.feature.followed.presentation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class FollowedChannelsViewModel(
    private val repository: TwitchRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    @Immutable
    data class State(
        val data: ImmutableList<ChannelFollow> = persistentListOf(),
        val isLoading: Boolean = true,
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var syncJob: Job? = null

    init {
        viewModelScope.launch {
            repository
                .getFollowedChannels()
                .collect { channels ->
                    _state.update { state ->
                        state.copy(
                            data = channels.toPersistentList(),
                        )
                    }
                }
        }
    }

    fun synchronize() {
        syncJob?.cancel()
        syncJob =
            viewModelScope.launch(DispatchersProvider.io) {
                _state.update { state ->
                    state.copy(isLoading = true)
                }

                repository.syncFollowedChannels(
                    appUser = authRepository.currentUser.first(),
                )

                _state.update { state ->
                    state.copy(isLoading = false)
                }
            }
    }
}
