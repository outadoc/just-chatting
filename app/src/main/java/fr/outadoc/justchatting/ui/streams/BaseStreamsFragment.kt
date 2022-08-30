package fr.outadoc.justchatting.ui.streams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.ui.common.BasePagedListAdapter
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.ui.common.PagedListFragment
import fr.outadoc.justchatting.ui.common.PagedListViewModel
import fr.outadoc.justchatting.ui.common.Scrollable
import kotlinx.android.synthetic.main.common_recycler_view_layout.recyclerView

abstract class BaseStreamsFragment<VM : PagedListViewModel<Stream>> :
    PagedListFragment<Stream, VM, BasePagedListAdapter<Stream>>(), Scrollable {

    override val adapter: BasePagedListAdapter<Stream> by lazy {
        StreamsCompactAdapter(activity as NavigationHandler)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_streams, container, false)
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }
}
