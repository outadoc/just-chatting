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
        val langValues = context.resources.getStringArray(R.array.gqlUserLanguageValues).toList()
        val language = if (languageIndex != 0) {
            langValues.elementAt(languageIndex)
        } else null
        if (it.useHelix) {
            repository.loadVideos(it.clientId, it.token, it.gameId, it.period, it.broadcastType, language?.lowercase(), it.sort, viewModelScope)
        } else {
            repository.loadGameVideosGQLQuery(it.clientId, it.gameId,
                if (language != null) {
                    val langList = mutableListOf<String>()
                    langList.add(language)
                    langList
                } else null,
                when (it.broadcastType) {
                    BroadcastType.ARCHIVE -> com.github.andreyasadchy.xtra.type.BroadcastType.ARCHIVE
                    BroadcastType.HIGHLIGHT -> com.github.andreyasadchy.xtra.type.BroadcastType.HIGHLIGHT
                    BroadcastType.UPLOAD -> com.github.andreyasadchy.xtra.type.BroadcastType.UPLOAD
                    else -> null },
                when (it.sort) { Sort.TIME -> VideoSort.TIME else -> VideoSort.VIEWS }, viewModelScope)
        }
    }
    val sort: Sort
        get() = filter.value!!.sort
    val period: Period
        get() = filter.value!!.period
    val type: BroadcastType
        get() = filter.value!!.broadcastType
    val languageIndex: Int
        get() = filter.value!!.languageIndex

    init {
        _sortText.value = context.getString(R.string.sort_and_period, context.getString(R.string.view_count), context.getString(R.string.this_week))
    }

    fun setGame(useHelix: Boolean, clientId: String?, gameId: String? = null, token: String? = null) {
        if (filter.value?.gameId != gameId) {
            filter.value = Filter(useHelix = useHelix, clientId = clientId, token = token, gameId = gameId)
        }
    }

    fun filter(useHelix: Boolean, clientId: String?, sort: Sort, period: Period, type: BroadcastType, languageIndex: Int, text: CharSequence, token: String? = null) {
        filter.value = filter.value?.copy(useHelix = useHelix, clientId = clientId, token = token, sort = sort, period = period, broadcastType = type, languageIndex = languageIndex)
        _sortText.value = text
    }

    private data class Filter(
        val useHelix: Boolean,
        val clientId: String?,
        val token: String?,
        val gameId: String?,
        val sort: Sort = Sort.VIEWS,
        val period: Period = Period.WEEK,
        val broadcastType: BroadcastType = BroadcastType.ALL,
        val languageIndex: Int = 0)
}
