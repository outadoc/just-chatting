package com.github.andreyasadchy.xtra.ui.streams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.NavigationHandler
import com.github.andreyasadchy.xtra.ui.common.PagedListFragment
import com.github.andreyasadchy.xtra.ui.common.PagedListViewModel
import com.github.andreyasadchy.xtra.ui.common.Scrollable
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
