package fr.outadoc.justchatting.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.repository.TwitchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

class FollowedStreamsViewModel(
    private val repository: TwitchRepository,
) : ViewModel() {

    private val _load = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingData: Flow<PagingData<Stream>> =
        _load.flatMapLatest { repository.loadFollowedStreams() }
            .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            _load.value = Random.nextInt()
        }
    }
}
