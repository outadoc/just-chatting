package fr.outadoc.justchatting.feature.timeline.presentation

import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.timeline.domain.model.FullSchedule
import fr.outadoc.justchatting.utils.presentation.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class EpgViewModel(
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

    private var job: Job? = null

    fun load() {
        job?.cancel()
        job = viewModelScope.launch {
            _state.update { state ->
                state.copy(isLoading = true)
            }

            val now = clock.now()
            val tz = _state.value.timeZone
            val today = now.toLocalDateTime(tz).date

            launch {
                twitchRepository.syncFollowedChannelsSchedule(
                    today = today,
                    timeZone = tz,
                )

                _state.update { state ->
                    state.copy(isLoading = false)
                }
            }

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
}
