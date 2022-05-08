package com.github.andreyasadchy.xtra.ui.follow.channels

import android.app.Application
import android.content.Context
import androidx.core.util.Pair
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.follows.Sort
import com.github.andreyasadchy.xtra.model.offline.SortChannel
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.SortChannelRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class FollowedChannelsViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService,
        private val sortChannelRepository: SortChannelRepository) : PagedListViewModel<Follow>() {

    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Follow>> = Transformations.map(filter) {
        repository.loadFollowedChannels(it.user.id, it.helixClientId, it.user.helixToken, it.gqlClientId, it.user.gqlToken, it.apiPref, it.sort, it.order, viewModelScope)
    }
    val sort: Sort
        get() = filter.value!!.sort
    val order: Order
        get() = filter.value!!.order

    fun setUser(context: Context, user: User, helixClientId: String?, gqlClientId: String?, apiPref: ArrayList<Pair<Long?, String?>?>) {
        if (filter.value == null) {
            val sortValues = runBlocking { sortChannelRepository.getById("followed_channels") }
            filter.value = Filter(
                user = user,
                helixClientId = helixClientId,
                gqlClientId = gqlClientId,
                apiPref = apiPref,
                sort = when (sortValues?.videoSort) {
                    Sort.FOLLOWED_AT.value -> Sort.FOLLOWED_AT
                    Sort.ALPHABETICALLY.value -> Sort.ALPHABETICALLY
                    else -> Sort.LAST_BROADCAST
                },
                order = when (sortValues?.videoType) {
                    Order.ASC.value -> Order.ASC
                    else -> Order.DESC
                }
            )
            _sortText.value = context.getString(R.string.sort_and_period,
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
            (sortDefaults?.apply {
                videoSort = sort.value
                videoType = order.value
            } ?: SortChannel(
                id = "followed_channels",
                videoSort = sort.value,
                videoType = order.value
            )).let { sortChannelRepository.save(it) }
        }
    }

    private data class Filter(
        val user: User,
        val helixClientId: String?,
        val gqlClientId: String?,
        val apiPref: ArrayList<Pair<Long?, String?>?>,
        val sort: Sort = Sort.LAST_BROADCAST,
        val order: Order = Order.DESC)
}
