package com.github.andreyasadchy.xtra.ui.follow.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.helix.follows.Follow
import com.github.andreyasadchy.xtra.model.helix.follows.Order
import com.github.andreyasadchy.xtra.model.helix.follows.Sort
import com.github.andreyasadchy.xtra.ui.common.BasePagedListAdapter
import com.github.andreyasadchy.xtra.ui.common.OnChannelSelectedListener
import com.github.andreyasadchy.xtra.ui.common.PagedListFragment
import com.github.andreyasadchy.xtra.ui.common.Scrollable
import kotlinx.android.synthetic.main.common_recycler_view_layout.recyclerView
import kotlinx.android.synthetic.main.fragment_followed_channels.sortBar
import kotlinx.android.synthetic.main.sort_bar.sortText

class FollowedChannelsFragment :
    PagedListFragment<Follow, FollowedChannelsViewModel, BasePagedListAdapter<Follow>>(),
    FollowedChannelsSortDialog.OnFilter,
    Scrollable {

    override val viewModel by viewModels<FollowedChannelsViewModel> { viewModelFactory }
    override val adapter: BasePagedListAdapter<Follow> by lazy {
        FollowedChannelsAdapter(activity as OnChannelSelectedListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_followed_channels, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.sortText.observe(viewLifecycleOwner) {
            sortText.text = it
        }

        viewModel.setUser(context = requireContext())

        sortBar.setOnClickListener {
            FollowedChannelsSortDialog.newInstance(sort = viewModel.sort, order = viewModel.order)
                .show(childFragmentManager, null)
        }
    }

    override fun onChange(
        sort: Sort,
        sortText: CharSequence,
        order: Order,
        orderText: CharSequence
    ) {
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
