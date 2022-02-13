package com.github.andreyasadchy.xtra.ui.search.tags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.helix.tag.Tag
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import javax.inject.Inject

class TagSearchViewModel @Inject constructor(
        private val repository: TwitchService) : PagedListViewModel<Tag>() {

    private val filter = MutableLiveData<Filter>()
    override val result: LiveData<Listing<Tag>> = Transformations.map(filter) {
        repository.loadTagsGQL(it.clientId, it.getGameTags, it.gameId, it.gameName, it.query, viewModelScope)
    }

    fun loadTags(clientId: String?, getGameTags: Boolean, gameId: String?, gameName: String?) {
        Filter(clientId, getGameTags, gameId, gameName).let {
            if (filter.value != it) {
                filter.value = it
            }
        }
    }

    fun setQuery(query: String) {
        if (filter.value?.query != query) {
            filter.value = filter.value?.copy(query = query)
        }
    }

    private data class Filter(
        val clientId: String?,
        val getGameTags: Boolean,
        val gameId: String?,
        val gameName: String?,
        val query: String? = null)
}