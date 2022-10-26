package fr.outadoc.justchatting.ui.search.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import fr.outadoc.justchatting.databinding.CommonRecyclerViewLayoutBinding
import fr.outadoc.justchatting.model.helix.channel.ChannelSearch
import fr.outadoc.justchatting.ui.common.BasePagedListAdapter
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.ui.common.PagedListFragment
import fr.outadoc.justchatting.ui.common.Scrollable
import fr.outadoc.justchatting.ui.search.Searchable
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
        viewHolder?.swipeRefresh?.isEnabled = false
    }

    override fun search(query: String) {
        if (query.isNotEmpty()) {
            viewModel.setQuery(query = query)
        } else {
            viewHolder?.nothingHere?.isVisible = false
        }
    }

    override fun scrollToTop() {
        viewHolder?.recyclerView?.scrollToPosition(0)
    }
}
