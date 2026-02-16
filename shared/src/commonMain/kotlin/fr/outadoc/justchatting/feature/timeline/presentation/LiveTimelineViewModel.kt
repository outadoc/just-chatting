package fr.outadoc.justchatting.feature.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

internal class LiveTimelineViewModel(
    private val twitchRepository: TwitchRepository,
    private val clock: Clock,
    authRepository: AuthRepository,
) : ViewModel() {
    data class State(
        val isLoading: Boolean = false,
        val live: ImmutableList<UserStream> = persistentListOf(),
        val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    )

    private val currentAppUser =
        authRepository.currentUser
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = AppUser.NotLoggedIn,
            )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var periodicSyncJob: Job? = null

    fun syncLiveStreamsPeriodically() {
        if (periodicSyncJob?.isActive == true) {
            return
        }

        periodicSyncJob =
            viewModelScope.launch(DispatchersProvider.default) {
                launch {
                    val tz = _state.value.timeZone
                    val today = clock.now().toLocalDateTime(tz).date

                    twitchRepository
                        .getFollowedChannelsSchedule(
                            today = today,
                            timeZone = tz,
                        ).collect { schedule ->
                            _state.update { state ->
                                state.copy(live = schedule.live)
                            }
                        }
                }

                while (isActive) {
                    doSync()
                    delay(1.minutes)
                }
            }
    }

    fun syncLiveStreamsNow() {
        viewModelScope.launch {
            doSync()
        }
    }

    private suspend fun doSync() {
        _state.update { state ->
            state.copy(isLoading = true)
        }

        twitchRepository.syncFollowedStreams(appUser = currentAppUser.value)

        _state.update { state ->
            state.copy(isLoading = false)
        }
    }
}
