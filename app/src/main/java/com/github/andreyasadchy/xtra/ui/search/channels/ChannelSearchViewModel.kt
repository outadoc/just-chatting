package com.github.andreyasadchy.xtra.ui.search.channels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.github.andreyasadchy.xtra.model.helix.channel.ChannelSearch
import com.github.andreyasadchy.xtra.repository.Listing
import com.github.andreyasadchy.xtra.repository.TwitchService
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import com.github.andreyasadchy.xtra.util.nullIfEmpty
import javax.inject.Inject

class ChannelSearchViewModel @Inject constructor(
    private val repository: TwitchService
) : PagedListViewModel<ChannelSearch>() {

    private val query = MutableLiveData<String>()
    private var helixClientId = MutableLiveData<String>()
    private var helixToken = MutableLiveData<String>()

    override val result: LiveData<Listing<ChannelSearch>> = Transformations.map(query) {
        repository.loadSearchChannels(
            query = it,
            helixClientId = helixClientId.value?.nullIfEmpty(),
            helixToken = helixToken.value?.nullIfEmpty(),
            coroutineScope = viewModelScope
        )
    }

    fun setQuery(
        query: String,
        helixClientId: String? = null,
        helixToken: String? = null
    ) {
        if (this.helixClientId.value != helixClientId) {
            this.helixClientId.value = helixClientId
        }
        if (this.helixToken.value != helixToken) {
            this.helixToken.value = helixToken
        }
        if (this.query.value != query) {
            this.query.value = query
        }
    }
}
