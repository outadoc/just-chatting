package fr.outadoc.justchatting.feature.home.presentation

import androidx.paging.PagingData
import androidx.paging.cachedIn
import fr.outadoc.justchatting.feature.home.domain.TwitchRepository
import fr.outadoc.justchatting.feature.home.domain.model.Stream
import fr.outadoc.justchatting.feature.home.domain.model.UserStream
import fr.outadoc.justchatting.utils.presentation.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlin.random.Random

internal class FollowedStreamsViewModel(
    private val repository: TwitchRepository,
) : ViewModel() {

    private val load = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pagingData: Flow<PagingData<UserStream>> =
        load.flatMapLatest { repository.getFollowedStreams() }
            .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            load.value = Random.nextInt()
        }
    }
}
