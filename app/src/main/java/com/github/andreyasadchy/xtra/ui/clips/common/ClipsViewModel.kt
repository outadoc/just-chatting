package com.github.andreyasadchy.xtra.ui.clips.common

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.model.helix.video.Period
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import javax.inject.Inject

class ClipsViewModel @Inject constructor(
        context: Application,
        private val repository: TwitchService) : PagedListViewModel<Clip>() {

    val sortOptions = listOf(R.string.trending, R.string.today, R.string.this_week, R.string.this_month, R.string.all_time)
    private val _sortText = MutableLiveData<CharSequence>()
    val sortText: LiveData<CharSequence>
        get() = _sortText
    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Clip>> = Transformations.map(filter) {
        if (it.usehelix) {
            val started = when (it.period) {
                Period.ALL -> null
                else -> TwitchApiHelper.getClipTime(it.period)
            }
            val ended = when (it.period) {
                Period.ALL -> null
                else -> TwitchApiHelper.getClipTime()
            }
            repository.loadClips(it.clientId, it.token, it.channelName, it.game, started, ended, viewModelScope)
        } else {
            val period = when (it.period) {
                Period.DAY -> "LAST_DAY"
                Period.WEEK -> "LAST_WEEK"
                Period.MONTH -> "LAST_MONTH"
                else -> "ALL_TIME" }
            if (it.game == null)
                repository.loadChannelClipsGQL(it.clientId, it.channelName, period, viewModelScope)
            else
                repository.loadGameClipsGQL(it.clientId, it.game, period, viewModelScope)
        }
    }
    var selectedIndex = 2
        private set

    init {
        _sortText.value = context.getString(sortOptions[selectedIndex])
    }

    fun loadClips(usehelix: Boolean, clientId: String?, channelName: String? = null, game: String? = null, token: String? = "") {
        if (filter.value == null) {
            filter.value = Filter(usehelix = usehelix, clientId = clientId, token = token, channelName = channelName, game = game)
        } else {
            filter.value?.copy(usehelix = usehelix, clientId = clientId, token = token, channelName = channelName, game = game).let {
                if (filter.value != it)
                    filter.value = it
            }
        }
    }

    fun filter(usehelix: Boolean, clientId: String?, period: Period?, index: Int, text: CharSequence, token: String? = "") {
        filter.value = filter.value?.copy(usehelix = usehelix, clientId = clientId, token = token, period = period)
        _sortText.value = text
        selectedIndex = index
    }

    private data class Filter(
            val usehelix: Boolean,
            val clientId: String?,
            val token: String?,
            val channelName: String?,
            val game: String?,
            val period: Period? = Period.WEEK)
}
