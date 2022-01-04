package com.github.andreyasadchy.xtra.ui.videos.channel


import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.video.BroadcastType
import com.github.andreyasadchy.xtra.model.helix.video.Period
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

    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Video>> = Transformations.map(filter) {
        if (it.usehelix)
            repository.loadChannelVideos(it.clientId, it.token, it.channelId, it.period, it.broadcastType, it.sort, viewModelScope)
        else
            repository.loadChannelVideosGQL(it.clientId, it.channelId,
                when (it.broadcastType) {
                    BroadcastType.ARCHIVE -> com.github.andreyasadchy.xtra.type.BroadcastType.ARCHIVE
                    BroadcastType.HIGHLIGHT -> com.github.andreyasadchy.xtra.type.BroadcastType.HIGHLIGHT
                    BroadcastType.UPLOAD -> com.github.andreyasadchy.xtra.type.BroadcastType.UPLOAD
                    else -> null },
                when (it.sort) { Sort.TIME -> VideoSort.TIME else -> VideoSort.VIEWS }, viewModelScope)
    }
    val sort: Sort
        get() = filter.value!!.sort
    val period: Period
        get() = filter.value!!.period
    val type: BroadcastType
        get() = filter.value!!.broadcastType

    init {
        _sortText.value = context.getString(R.string.sort_and_period, context.getString(R.string.upload_date), context.getString(R.string.all_time))
    }

    fun setChannelId(usehelix: Boolean, clientId: String?, channelId: String, token: String? = null) {
        if (filter.value?.channelId != channelId) {
            filter.value = Filter(usehelix, clientId, token, channelId = channelId)
        }
    }

    fun filter(usehelix: Boolean, clientId: String?, sort: Sort, period: Period, type: BroadcastType, text: CharSequence, token: String? = null) {
        filter.value = filter.value?.copy(usehelix = usehelix, clientId = clientId, token = token, sort = sort, period = period, broadcastType = type)
        _sortText.value = text
    }

    private data class Filter(
            val usehelix: Boolean,
            val clientId: String?,
            val token: String?,
            val channelId: String,
            val sort: Sort = Sort.TIME,
            val period: Period = Period.ALL,
            val broadcastType: BroadcastType = BroadcastType.ALL)
}
