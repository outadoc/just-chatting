package fr.outadoc.justchatting.ui.streams

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
import fr.outadoc.justchatting.ui.common.PagedListViewModel
import fr.outadoc.justchatting.ui.common.Scrollable

abstract class BaseStreamsFragment<VM : PagedListViewModel<Stream>> :
    PagedListFragment<Stream, VM, BasePagedListAdapter<Stream>>(), Scrollable {

    override val adapter: BasePagedListAdapter<Stream> by lazy {
        StreamsCompactAdapter(activity as NavigationHandler)
    }

    var viewHolder: FragmentStreamsBinding? = null

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
}
