package fr.outadoc.justchatting.ui.follow.channels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.model.helix.follows.Order
import fr.outadoc.justchatting.model.helix.follows.Sort
import fr.outadoc.justchatting.model.offline.SortChannel
import fr.outadoc.justchatting.repository.Listing
import fr.outadoc.justchatting.repository.SortChannelRepository
import fr.outadoc.justchatting.repository.TwitchService
import fr.outadoc.justchatting.ui.common.PagedListViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FollowedChannelsViewModel(
    private val repository: TwitchService,
    private val sortChannelRepository: SortChannelRepository,
) : PagedListViewModel<Follow>() {

    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText

    private val filter = MutableStateFlow<Filter?>(null)

    private val _result: Flow<Listing<Follow>> =
        filter.filterNotNull()
            .map { filter ->
                repository.loadFollowedChannels(
                    sort = filter.sort,
                    order = filter.order,
                    coroutineScope = viewModelScope
                )
            }

    override val result: LiveData<Listing<Follow>> = _result.asLiveData()

    val sort: Sort
        get() = filter.value!!.sort

    val order: Order
        get() = filter.value!!.order

    fun setUser(context: Context) = viewModelScope.launch {
        if (filter.value == null) {
            val sortValues = runBlocking { sortChannelRepository.getById("followed_channels") }
            filter.value = Filter(
                sort = when (sortValues?.videoSort) {
                    Sort.FOLLOWED_AT.value -> Sort.FOLLOWED_AT
                    else -> Sort.ALPHABETICALLY
                },
                order = when (sortValues?.videoType) {
                    Order.ASC.value -> Order.ASC
                    else -> Order.DESC
                }
            )

            _sortText.value = context.getString(
                R.string.sort_and_period,
                when (sortValues?.videoSort) {
                    Sort.FOLLOWED_AT.value -> context.getString(R.string.time_followed)
                    Sort.ALPHABETICALLY.value -> context.getString(R.string.alphabetically)
                    else -> context.getString(R.string.last_broadcast)
                },
                when (sortValues?.videoType) {
                    Order.ASC.value -> context.getString(R.string.ascending)
                    else -> context.getString(R.string.descending)
                }
            )
        }
    }

    fun filter(sort: Sort, order: Order, text: CharSequence) {
        filter.value = filter.value?.copy(sort = sort, order = order)
        _sortText.value = text

        viewModelScope.launch {
            val sortDefaults = sortChannelRepository.getById("followed_channels")
            (
                sortDefaults?.apply {
                    videoSort = sort.value
                    videoType = order.value
                } ?: SortChannel(
                    id = "followed_channels",
                    videoSort = sort.value,
                    videoType = order.value
                )
                ).let { sortChannelRepository.save(it) }
        }
    }

    private data class Filter(
        val sort: Sort = Sort.FOLLOWED_AT,
        val order: Order = Order.DESC
    )
}
