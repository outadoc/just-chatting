package fr.outadoc.justchatting.feature.home.presentation

import androidx.paging.PagingData
import fr.outadoc.justchatting.feature.home.domain.GetScheduleForFollowedChannelsUseCase
import fr.outadoc.justchatting.feature.home.domain.model.ChannelSchedule
import fr.outadoc.justchatting.utils.presentation.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class EpgViewModel(
    private val getScheduleForFollowedChannels: GetScheduleForFollowedChannelsUseCase,
) : ViewModel() {

    sealed class State {
        data object Loading : State()
        data class Loaded(
            val pagingData: Flow<PagingData<ChannelSchedule>>,
        ) : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.value = State.Loading
            _state.value = State.Loaded(
                pagingData = getScheduleForFollowedChannels(),
            )
        }
    }
}
