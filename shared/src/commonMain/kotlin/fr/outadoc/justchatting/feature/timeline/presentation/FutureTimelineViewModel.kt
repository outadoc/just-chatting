package fr.outadoc.justchatting.feature.timeline.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.TimeZoneProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

public class FutureTimelineViewModel internal constructor(
    private val twitchRepository: TwitchRepository,
    private val clock: Clock,
    private val authRepository: AuthRepository,
    private val dispatchersProvider: DispatchersProvider,
    private val timeZoneProvider: TimeZoneProvider,
) : ViewModel() {
    public data class State(
        val isLoading: Boolean = false,
        val days: ImmutableList<ScheduleDay> = persistentListOf(),
    )

    private val _state = MutableStateFlow(State())
    public val state: StateFlow<State> = _state.asStateFlow()

    /**
     * Read every time it's needed, since the user can change their time zone at any time.
     */
    private val timeZone: TimeZone
        get() = timeZoneProvider.currentTimeZone

    private var syncJob: Job? = null

    /**
     * The current time, updated every minute so that what's happening now stays up to date.
     */
    private val now: Flow<Instant> =
        flow {
            while (currentCoroutineContext().isActive) {
                emit(clock.now())
                delay(1.minutes)
            }
        }

    init {
        viewModelScope.launch {
            val today = clock.now().toLocalDateTime(timeZone).date

            combine(
                twitchRepository.getFollowedChannelsSchedule(
                    today = today,
                    timeZone = timeZone,
                ),
                now,
            ) { schedule, now ->
                schedule.future.toScheduleDays(now = now, timeZone = timeZone)
            }.collect { days ->
                _state.update { state ->
                    state.copy(days = days)
                }
            }
        }
    }

    public fun syncLiveStreamsNow() {
        viewModelScope.launch(dispatchersProvider.io) {
            twitchRepository.syncFollowedStreams(
                appUser = authRepository.currentUser.first(),
            )
        }
    }

    public fun syncEverythingNow() {
        syncJob?.cancel()
        syncJob =
            viewModelScope.launch(dispatchersProvider.io) {
                _state.update { state ->
                    state.copy(isLoading = true)
                }

                val today = clock.now().toLocalDateTime(timeZone).date

                twitchRepository.syncFollowedChannelsSchedule(
                    today = today,
                    timeZone = timeZone,
                    appUser = authRepository.currentUser.first(),
                )

                _state.update { state ->
                    state.copy(isLoading = false)
                }
            }
    }
}
