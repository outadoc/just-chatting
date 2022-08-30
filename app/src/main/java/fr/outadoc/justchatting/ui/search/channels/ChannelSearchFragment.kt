package fr.outadoc.justchatting.ui.search.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.helix.channel.ChannelSearch
import fr.outadoc.justchatting.ui.common.BasePagedListAdapter
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.ui.common.PagedListFragment
import fr.outadoc.justchatting.ui.common.Scrollable
import fr.outadoc.justchatting.ui.search.Searchable
import kotlinx.android.synthetic.main.common_recycler_view_layout.nothingHere
import kotlinx.android.synthetic.main.common_recycler_view_layout.recyclerView
import kotlinx.android.synthetic.main.common_recycler_view_layout.swipeRefresh

class ChannelSearchFragment :
    PagedListFragment<ChannelSearch, ChannelSearchViewModel, BasePagedListAdapter<ChannelSearch>>(),
    Searchable,
    Scrollable {

    override val viewModel by viewModels<ChannelSearchViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<ChannelSearch> by lazy {
        ChannelSearchAdapter(activity as NavigationHandler)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.common_recycler_view_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefresh.isEnabled = false
    }

    override fun search(query: String) {
        if (query.isNotEmpty()) {
            viewModel.setQuery(query = query)
        } else {
            adapter.submitList(null)
            nothingHere?.isVisible = false
        }
    }

    override fun scrollToTop() {
        recyclerView.scrollToPosition(0)
    }
}
