package fr.outadoc.justchatting.ui.streams.followed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.outadoc.justchatting.databinding.CommonRecyclerViewLayoutBinding
import fr.outadoc.justchatting.databinding.FragmentStreamsBinding
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.ui.common.BasePagedListAdapter
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.ui.common.PagedListFragment
import fr.outadoc.justchatting.ui.common.Scrollable
import fr.outadoc.justchatting.ui.streams.StreamsCompactAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FollowedStreamsFragment :
    PagedListFragment<Stream, FollowedStreamsViewModel, BasePagedListAdapter<Stream>>(),
    Scrollable {

    override val viewModel: FollowedStreamsViewModel by viewModel()

    var viewHolder: FragmentStreamsBinding? = null

    override val adapter: BasePagedListAdapter<Stream> by lazy {
        StreamsCompactAdapter(activity as NavigationHandler)
    }

    override val commonViewHolder: CommonRecyclerViewLayoutBinding?
        get() = viewHolder?.layoutRecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewHolder = FragmentStreamsBinding.inflate(inflater, container, false)
        return viewHolder?.root
    }

    override fun scrollToTop() {
        commonViewHolder?.recyclerView?.scrollToPosition(0)
    }

    override fun onResume() {
        super.onResume()
        adapter.refresh()
    }
}
