package com.github.andreyasadchy.xtra.ui.videos.channel


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Sort
import com.github.andreyasadchy.xtra.model.helix.video.Video
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.PlayerRepository
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.type.VideoSort
import com.github.andreyasadchy.xtra.ui.videos.BaseVideosViewModel
import javax.inject.Inject

class ChannelVideosViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService,
        playerRepository: PlayerRepository) : BaseVideosViewModel(playerRepository) {

    val sortOptions = listOf(R.string.upload_date, R.string.view_count)
    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Video>> = Transformations.map(filter) {
        if (it.usehelix)
            repository.loadChannelVideos(it.clientId, it.token, it.channelId, BroadcastType.ALL, it.sort, viewModelScope)
        else
            repository.loadChannelVideosGQL(it.clientId, it.channelId, null, when (it.sort) { Sort.TIME -> VideoSort.TIME else -> VideoSort.VIEWS }, viewModelScope)
    }
    var selectedIndex = 0
        private set

    init {
        _sortText.value = context.getString(sortOptions[selectedIndex])
    }

    fun setChannelId(usehelix: Boolean, clientId: String?, channelId: String, token: String? = "") {
        if (filter.value?.channelId != channelId) {
            filter.value = Filter(usehelix, clientId, token, channelId = channelId)
        }
    }

    fun setSort(usehelix: Boolean, clientId: String?, sort: Sort, index: Int, text: CharSequence, token: String? = "") {
        filter.value = filter.value?.copy(usehelix = usehelix, clientId = clientId, token = token, sort = sort)
        selectedIndex = index
        _sortText.value = text
    }

    private data class Filter(
            val usehelix: Boolean,
            val clientId: String?,
            val token: String?,
            val channelId: String,
            val sort: Sort = Sort.TIME)
}
