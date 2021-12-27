package com.github.andreyasadchy.xtra.ui.videos.game


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

class GameVideosViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService,
        playerRepository: PlayerRepository) : BaseVideosViewModel(playerRepository) {

    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Video>> = Transformations.map(filter) {
        if (it.usehelix)
            repository.loadVideos(it.clientId, it.token, it.gameId, it.period, it.broadcastType, it.language, it.sort, viewModelScope)
        else
            repository.loadGameVideosGQL(it.clientId, it.gameId, it.gameName,
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
        _sortText.value = context.getString(R.string.sort_and_period, context.getString(R.string.view_count), context.getString(R.string.this_week))
    }

    fun setGame(usehelix: Boolean, clientId: String?, gameId: String? = null, gameName: String? = null, token: String? = "") {
        if (filter.value?.gameId != gameId) {
            filter.value = Filter(usehelix = usehelix, clientId = clientId, token = token, gameId = gameId, gameName = gameName
            )
        }
    }

    fun filter(usehelix: Boolean, clientId: String?, sort: Sort, period: Period, type: BroadcastType, text: CharSequence, token: String? = "") {
        filter.value = filter.value?.copy(usehelix = usehelix, clientId = clientId, token = token, sort = sort, period = period, broadcastType = type)
        _sortText.value = text
    }

    private data class Filter(
            val usehelix: Boolean,
            val clientId: String?,
            val token: String?,
            val gameId: String?,
            val gameName: String?,
            val sort: Sort = Sort.VIEWS,
            val period: Period = Period.WEEK,
            val broadcastType: BroadcastType = BroadcastType.ALL,
            val language: String? = null)
}
