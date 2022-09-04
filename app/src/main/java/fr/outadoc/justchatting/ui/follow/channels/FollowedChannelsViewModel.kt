package fr.outadoc.justchatting.ui.follow.channels

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.model.helix.follows.Order
import fr.outadoc.justchatting.model.helix.follows.Sort
import fr.outadoc.justchatting.repository.Listing
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.ui.common.PagedListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FollowedChannelsViewModel(
    private val repository: TwitchService
) : PagedListViewModel<Follow>() {

    data class Filter(
        val sort: Sort = Sort.ALPHABETICALLY,
        val order: Order = Order.ASC
    )

    private val _filter = MutableStateFlow(Filter())
    val filter = _filter.asLiveData()

    override val result: LiveData<Listing<Follow>> =
        _filter.filterNotNull()
            .map { filter ->
                repository.loadFollowedChannels(
                    sort = filter.sort,
                    order = filter.order,
                    coroutineScope = viewModelScope
                )
            }
            .asLiveData()

    fun filter(sort: Sort, order: Order) {
        _filter.update { filter ->
            filter.copy(sort = sort, order = order)
        }
    }
}
