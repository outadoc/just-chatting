package fr.outadoc.justchatting.feature.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.timeline.domain.model.UserStream
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

public class LiveTimelineViewModel internal constructor(
    private val twitchRepository: TwitchRepository,
    private val clock: Clock,
    private val authRepository: AuthRepository,
) : ViewModel() {
    public sealed class Event {
        public data class NavigateToChannel(
            val userId: String,
        ) : Event()
    }

    public data class State(
        val isLoading: Boolean = false,
        val live: ImmutableList<UserStream> = persistentListOf(),
        val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    )

    private val _events = MutableSharedFlow<Event>()
    public val events: SharedFlow<Event> = _events.asSharedFlow()

    private val _state = MutableStateFlow(State())
    public val state: StateFlow<State> = _state.asStateFlow()

    private var periodicSyncJob: Job? = null

    public fun onChannelClick(userId: String) {
        viewModelScope.launch {
            _events.emit(Event.NavigateToChannel(userId))
        }
    }

    public fun syncLiveStreamsPeriodically() {
        if (periodicSyncJob?.isActive == true) {
            return
        }

        viewModelScope.launch {
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

        periodicSyncJob =
            viewModelScope.launch {
                while (isActive) {
                    doSync()
                    delay(1.minutes)
                }
            }
    }

    public fun syncLiveStreamsNow() {
        viewModelScope.launch {
            doSync()
        }
    }

    private suspend fun doSync() {
        _state.update { state ->
            state.copy(isLoading = true)
        }

        val appUser = authRepository.currentUser.first()

        twitchRepository.syncFollowedChannels(appUser)
        twitchRepository.syncFollowedStreams(appUser)

        _state.update { state ->
            state.copy(isLoading = false)
        }
    }
}
