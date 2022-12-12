package fr.outadoc.justchatting.ui.streams.followed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import fr.outadoc.justchatting.component.twitch.domain.api.TwitchRepository
import fr.outadoc.justchatting.component.twitch.model.Stream
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random

class FollowedStreamsViewModel(
    private val repository: TwitchRepository
) : ViewModel() {

    private val _load = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingData: Flow<PagingData<Stream>> =
        _load.flatMapLatest {
            repository.loadFollowedStreams()
                .flow
                .map { page ->
                    page.flatMap { streamsResponse ->
                        streamsResponse.data.orEmpty()
                            .associateBy { stream -> stream.userId }
                            .values
                            .let { stream -> repository.mapStreamsWithUserProfileImages(stream) }
                    }
                }
        }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            _load.value = Random.nextInt()
        }
    }
}
