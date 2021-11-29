package com.github.andreyasadchy.xtra.ui.follow.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.PagedListFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.common_recycler_view_layout.*

class FollowedChannelsFragment : PagedListFragment<Follow, FollowedChannelsViewModel, BasePagedListAdapter<Follow>>(), Scrollable {

    override val viewModel by viewModels<FollowedChannelsViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<Follow> by lazy {
        val activity = requireActivity() as MainActivity
        FollowedChannelsAdapter(this, activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_followed_channels, container, false)
    }

    override fun initialize() {
        super.initialize()
        viewModel.setUser(requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), User.get(requireContext()))
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }
}