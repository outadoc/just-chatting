package fr.outadoc.justchatting.feature.timeline.presentation

import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.timeline.domain.model.FullSchedule
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.presentation.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.minutes

internal class TimelineViewModel(
    private val twitchRepository: TwitchRepository,
    private val clock: Clock,
) : ViewModel() {

    data class State(
        val isLoading: Boolean = false,
        val schedule: FullSchedule = FullSchedule(),
        val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    )

    private val _state = MutableStateFlow(State())
    val state = _state.asStateFlow()

    private var periodicSyncJob: Job? = null
    private var syncJob: Job? = null

    init {
        viewModelScope.launch {
            val tz = _state.value.timeZone
            val today = clock.now().toLocalDateTime(tz).date

            twitchRepository
                .getFollowedChannelsSchedule(
                    today = today,
                    timeZone = tz,
                )
                .collect { schedule ->
                    _state.update { state ->
                        state.copy(schedule = schedule)
                    }
                }
        }
    }

    fun syncPeriodically() {
        if (periodicSyncJob?.isActive == true) {
            return
        }

        periodicSyncJob = viewModelScope.launch(DispatchersProvider.default) {
            while (isActive) {
                if (syncJob?.isActive != true) {
                    synchronize()
                }

                delay(1.minutes)
            }
        }
    }

    fun synchronize() {
        syncJob?.cancel()
        syncJob = viewModelScope.launch(DispatchersProvider.io) {
            _state.update { state ->
                state.copy(isLoading = true)
            }

            val tz = _state.value.timeZone
            val today = clock.now().toLocalDateTime(tz).date

            twitchRepository.syncFollowedChannelsSchedule(
                today = today,
                timeZone = tz,
            )

            _state.update { state ->
                state.copy(isLoading = false)
            }
        }

    }
}
