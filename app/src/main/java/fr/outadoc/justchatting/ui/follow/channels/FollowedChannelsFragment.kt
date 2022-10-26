package fr.outadoc.justchatting.ui.follow.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.outadoc.justchatting.databinding.CommonRecyclerViewLayoutBinding
import fr.outadoc.justchatting.databinding.FragmentFollowedChannelsBinding
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.ui.common.BasePagedListAdapter
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.ui.common.PagedListFragment
import fr.outadoc.justchatting.ui.common.Scrollable
import org.koin.androidx.viewmodel.ext.android.viewModel

class FollowedChannelsFragment :
    PagedListFragment<Follow, FollowedChannelsViewModel, BasePagedListAdapter<Follow>>(),
    Scrollable {

    override val viewModel: FollowedChannelsViewModel by viewModel()
    private var viewHolder: FragmentFollowedChannelsBinding? = null

    override val commonViewHolder: CommonRecyclerViewLayoutBinding?
        get() = viewHolder?.layoutRecyclerView

    override val adapter: BasePagedListAdapter<Follow> by lazy {
        FollowedChannelsAdapter(activity as NavigationHandler)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewHolder = FragmentFollowedChannelsBinding.inflate(inflater, container, false)
        return viewHolder?.root
    }

    override fun onResume() {
        super.onResume()
        adapter.refresh()
    }

    override fun scrollToTop() {
        viewHolder?.layoutRecyclerView?.recyclerView?.scrollToPosition(0)
    }
}
