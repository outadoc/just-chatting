package fr.outadoc.justchatting.ui.search.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import fr.outadoc.justchatting.databinding.CommonRecyclerViewLayoutBinding
import fr.outadoc.justchatting.model.helix.channel.ChannelSearch
import fr.outadoc.justchatting.ui.common.BasePagedListAdapter
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.ui.common.PagedListFragment
import fr.outadoc.justchatting.ui.common.Scrollable
import fr.outadoc.justchatting.ui.search.Searchable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChannelSearchFragment :
    PagedListFragment<ChannelSearch, ChannelSearchViewModel, BasePagedListAdapter<ChannelSearch>>(),
    Searchable,
    Scrollable {

    override val viewModel: ChannelSearchViewModel by viewModel()

    var viewHolder: CommonRecyclerViewLayoutBinding? = null

    override val commonViewHolder: CommonRecyclerViewLayoutBinding?
        get() = viewHolder

    override val adapter: BasePagedListAdapter<ChannelSearch> by lazy {
        ChannelSearchAdapter(activity as NavigationHandler)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewHolder = CommonRecyclerViewLayoutBinding.inflate(inflater, container, false)
        return viewHolder?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                commonViewHolder?.apply {
                    swipeRefresh.isRefreshing = loadStates.refresh is LoadState.Loading
                    nothingHere.isVisible =
                        loadStates.source.refresh is LoadState.NotLoading &&
                                loadStates.append.endOfPaginationReached &&
                                adapter.itemCount < 1
                }
            }
        }
    }

    override fun search(query: String) {
        viewModel.onQueryChange(query = query)
    }

    override fun scrollToTop() {
        viewHolder?.recyclerView?.scrollToPosition(0)
    }
}
