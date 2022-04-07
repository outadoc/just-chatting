package com.github.andreyasadchy.xtra.ui.videos.offline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.offline.OfflineVideo
import com.github.andreyasadchy.xtra.ui.common.PagedListFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.common_recycler_view_layout.*
import kotlinx.android.synthetic.main.fragment_games.*

abstract class BaseOfflineVideosFragment<VM : BaseOfflineViewModel> : PagedListFragment<OfflineVideo, VM, BaseOfflineVideosAdapter>(), Scrollable {

    var lastSelectedItem: OfflineVideo? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_downloads, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefresh.isEnabled = false
        scrollTop.isEnabled = false
        recyclerView.clearOnScrollListeners()
    }

    override fun initialize() {
        super.initialize()
        if (requireContext().prefs().getBoolean(C.PLAYER_USE_VIDEOPOSITIONS, true)) {
            viewModel.positions.observe(viewLifecycleOwner) {
                adapter.setVideoPositions(it)
            }
        }
    }

    override fun scrollToTop() {
        appBar?.setExpanded(true, true)
        recyclerView?.scrollToPosition(0)
    }
}