package com.github.andreyasadchy.xtra.ui.clips.common

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.clip.Clip
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
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
        repository.loadClips(it.clientId, it.token, it.channelName, it.game, viewModelScope)
    }
    var selectedIndex = 2
        private set

    init {
        _sortText.value = context.getString(sortOptions[selectedIndex])
    }

    fun loadClips(clientId: String?, token: String?, channelName: String? = null, game: String? = null) {
        if (filter.value == null) {
            filter.value = Filter(clientId, token, channelName, game)
        } else {
            filter.value?.copy(clientId = clientId, channelName = channelName, game = game).let {
                if (filter.value != it)
                    filter.value = it
            }
        }
    }

    fun filter(index: Int, text: CharSequence) {
        _sortText.value = text
        selectedIndex = index
    }

    private data class Filter(
            val clientId: String?,
            val token: String?,
            val channelName: String?,
            val game: String?)
}
