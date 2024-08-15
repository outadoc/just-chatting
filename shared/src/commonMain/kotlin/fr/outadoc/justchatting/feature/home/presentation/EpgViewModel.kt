package fr.outadoc.justchatting.feature.home.presentation

import androidx.paging.cachedIn
import fr.outadoc.justchatting.feature.home.domain.EpgConfig
import fr.outadoc.justchatting.feature.home.domain.GetScheduleForFollowedChannelsUseCase
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSchedule
import fr.outadoc.justchatting.utils.presentation.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

internal class EpgViewModel(
    private val getScheduleForFollowedChannels: GetScheduleForFollowedChannelsUseCase,
    private val clock: Clock,
) : ViewModel() {

    sealed class State {
        data object Loading : State()
        data class Loaded(
            val channels: List<ChannelSchedule>,
            val initialListIndex: Int,
            val days: List<LocalDate>,
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

            val tz = TimeZone.currentSystemDefault()

            val content: Flow<List<ChannelSchedule>> =
                getScheduleForFollowedChannels(
                    currentTime = clock.now(),
                    timeZone = tz,
                )
                    .map { pagingData ->
                        pagingData.map { schedule ->
                            schedule.copy(
                                scheduleFlow = schedule.scheduleFlow.cachedIn(viewModelScope),
                            )
                        }
                    }

            // Build a list of all days between today and the maximum number of days ahead
            val now = clock.now()
            val today = now.toLocalDateTime(tz).date

            val start = today - EpgConfig.MaxDaysAhead
            val end = today + EpgConfig.MaxDaysAhead

            val days: List<LocalDate> =
                (start.toEpochDays()..end.toEpochDays())
                    .map { LocalDate.fromEpochDays(it) }

            content.collect { channels ->
                _state.value = State.Loaded(
                    channels = channels,
                    days = days,
                    timeZone = tz,
                    initialListIndex = days.indexOf(today),
                )
            }
        }
    }
}
