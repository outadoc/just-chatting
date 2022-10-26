package fr.outadoc.justchatting.ui.follow.channels

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.flatMap
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.model.helix.follows.Order
import fr.outadoc.justchatting.model.helix.follows.Sort
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.ui.common.PagedListViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FollowedChannelsViewModel(
    private val repository: TwitchService,
) : PagedListViewModel<Follow>() {

    data class Filter(
        val sort: Sort = Sort.ALPHABETICALLY,
        val order: Order = Order.ASC
    )

    private val _filter = MutableStateFlow(Filter())
    val filter = _filter.asLiveData()

    override val pagingData: Flow<PagingData<Follow>> =
        _filter.filterNotNull()
            .flatMapLatest { filter ->
                repository.loadFollowedChannels()
                    .flow
                    .map { page ->
                        page.flatMap { followResponse ->
                            followResponse.data.orEmpty()
                                .let { follows -> repository.mapFollowsWithUserProfileImages(follows) }
                                .run {
                                    when (filter.order) {
                                        Order.ASC -> when (filter.sort) {
                                            Sort.FOLLOWED_AT -> sortedBy { it.followedAt }
                                            else -> sortedBy { it.toLogin }
                                        }

                                        Order.DESC -> when (filter.sort) {
                                            Sort.FOLLOWED_AT -> sortedByDescending { it.followedAt }
                                            else -> sortedByDescending { it.toLogin }
                                        }
                                    }
                                }
                        }
                    }
            }
            .cachedIn(viewModelScope)

    fun updateFilter(sort: Sort, order: Order) {
        _filter.update { filter ->
            filter.copy(sort = sort, order = order)
        }
    }
}
