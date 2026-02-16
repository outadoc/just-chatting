package fr.outadoc.justchatting.feature.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

internal class FutureTimelineViewModel(
    private val twitchRepository: TwitchRepository,
    private val clock: Clock,
    private val authRepository: AuthRepository,
) : ViewModel() {
    data class State(
        val isLoading: Boolean = false,
        val future: ImmutableMap<LocalDate, List<ChannelScheduleSegment>> = persistentMapOf(),
        val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var syncJob: Job? = null

    init {
        viewModelScope.launch {
            val tz = _state.value.timeZone
            val today = clock.now().toLocalDateTime(tz).date

            twitchRepository
                .getFollowedChannelsSchedule(
                    today = today,
                    timeZone = tz,
                ).collect { schedule ->
                    _state.update { state ->
                        state.copy(future = schedule.future)
                    }
                }
        }
    }

    fun syncLiveStreamsNow() {
        viewModelScope.launch(DispatchersProvider.io) {
            twitchRepository.syncFollowedStreams(
                appUser = authRepository.currentUser.first(),
            )
        }
    }

    fun syncEverythingNow() {
        syncJob?.cancel()
        syncJob =
            viewModelScope.launch(DispatchersProvider.io) {
                _state.update { state ->
                    state.copy(isLoading = true)
                }

                val tz = _state.value.timeZone
                val today = clock.now().toLocalDateTime(tz).date

                twitchRepository.syncFollowedChannelsSchedule(
                    today = today,
                    timeZone = tz,
                    appUser = authRepository.currentUser.first(),
                )

                _state.update { state ->
                    state.copy(isLoading = false)
                }
            }
    }
}
