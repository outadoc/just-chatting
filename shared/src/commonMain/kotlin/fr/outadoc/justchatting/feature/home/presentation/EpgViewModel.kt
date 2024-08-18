package fr.outadoc.justchatting.feature.home.presentation

import fr.outadoc.justchatting.feature.home.domain.TwitchRepository
import fr.outadoc.justchatting.feature.home.domain.model.FullSchedule
import fr.outadoc.justchatting.utils.presentation.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class EpgViewModel(
    private val twitchRepository: TwitchRepository,
    private val clock: Clock,
) : ViewModel() {

    sealed class State {
        data object Loading : State()
        data class Loaded(
            val schedule: FullSchedule,
            val initialListIndex: Int,
            val timeZone: TimeZone,
        ) : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    private var job: Job? = null

    fun load() {
        job?.cancel()
        job = viewModelScope.launch {
            _state.value = State.Loading

            val now = clock.now()
            val tz = TimeZone.currentSystemDefault()
            val today = now.toLocalDateTime(tz).date

            twitchRepository
                .getFollowedChannelsSchedule(
                    today = today,
                    timeZone = tz,
                )
                .collect { schedule ->
                    _state.value = State.Loaded(
                        schedule = schedule,
                        timeZone = tz,
                        initialListIndex = schedule.past.size,
                    )
                }
        }
    }
}
