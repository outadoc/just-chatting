package com.github.andreyasadchy.xtra.ui.follow.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.follows.Sort
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.PagedListFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.common_recycler_view_layout.*
import kotlinx.android.synthetic.main.fragment_followed_channels.*
import kotlinx.android.synthetic.main.sort_bar.*

class FollowedChannelsFragment : PagedListFragment<Follow, FollowedChannelsViewModel, BasePagedListAdapter<Follow>>(), FollowedChannelsSortDialog.OnFilter, Scrollable {

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
        viewModel.sortText.observe(viewLifecycleOwner) {
            sortText.text = it
        }
        viewModel.setUser(
            context = requireContext(),
            user = User.get(requireContext()),
            helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""),
            gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""),
            apiPref = TwitchApiHelper.listFromPrefs(requireContext().prefs().getString(C.API_PREF_FOLLOWED_CHANNELS, ""), TwitchApiHelper.followedChannelsApiDefaults),
        )
        sortBar.setOnClickListener { FollowedChannelsSortDialog.newInstance(viewModel.sort, viewModel.order).show(childFragmentManager, null) }
    }

    override fun onChange(sort: Sort, sortText: CharSequence, order: Order, orderText: CharSequence) {
        adapter.submitList(null)
        viewModel.filter(
            sort = sort,
            order = order,
            text = getString(R.string.sort_and_order, sortText, orderText)
        )
    }

    override fun scrollToTop() {
        recyclerView?.scrollToPosition(0)
    }
}