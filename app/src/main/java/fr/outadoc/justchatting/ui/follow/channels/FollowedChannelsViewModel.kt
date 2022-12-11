package fr.outadoc.justchatting.ui.follow.channels

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import fr.outadoc.justchatting.component.twitch.model.Follow
import fr.outadoc.justchatting.component.twitch.domain.api.TwitchRepository
import fr.outadoc.justchatting.ui.common.PagedListViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random

class FollowedChannelsViewModel(
    private val repository: TwitchRepository,
) : PagedListViewModel<Follow>() {

    private val _load = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val pagingData: Flow<PagingData<Follow>> =
        _load.flatMapLatest {
            repository.loadFollowedChannels()
                .flow
                .map { page ->
                    page.flatMap { followResponse ->
                        followResponse.data.orEmpty()
                            .let { follows -> repository.mapFollowsWithUserProfileImages(follows) }
                    }
                }
        }
            .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            _load.value = Random.nextInt()
        }
    }
}
