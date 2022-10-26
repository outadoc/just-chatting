package fr.outadoc.justchatting.ui.follow.channels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.CommonRecyclerViewLayoutBinding
import fr.outadoc.justchatting.databinding.FragmentFollowedChannelsBinding
import fr.outadoc.justchatting.model.helix.follows.Follow
import fr.outadoc.justchatting.model.helix.follows.Order
import fr.outadoc.justchatting.model.helix.follows.Sort
import fr.outadoc.justchatting.ui.common.BasePagedListAdapter
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.ui.common.PagedListFragment
import fr.outadoc.justchatting.ui.common.Scrollable
import org.koin.androidx.viewmodel.ext.android.viewModel

class FollowedChannelsFragment :
    PagedListFragment<Follow, FollowedChannelsViewModel, BasePagedListAdapter<Follow>>(),
    FollowedChannelsSortDialog.OnFilter,
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.filter.observe(viewLifecycleOwner) { filter ->
            val context = context ?: return@observe
            viewHolder?.apply {
                sortBar.sortText.text = context.getString(
                    R.string.sort_and_period,
                    when (filter.sort) {
                        Sort.FOLLOWED_AT -> context.getString(R.string.time_followed)
                        Sort.ALPHABETICALLY -> context.getString(R.string.alphabetically)
                    },
                    when (filter.order) {
                        Order.ASC -> context.getString(R.string.ascending)
                        Order.DESC -> context.getString(R.string.descending)
                    }
                )
            }
        }

        viewHolder?.sortBar?.root?.setOnClickListener {
            viewModel.filter.value?.let { filter ->
                FollowedChannelsSortDialog.newInstance(
                    sort = filter.sort,
                    order = filter.order
                ).show(
                    childFragmentManager,
                    null
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        adapter.refresh()
    }

    override fun onChange(
        sort: Sort,
        sortText: CharSequence,
        order: Order,
        orderText: CharSequence
    ) {
        viewModel.updateFilter(
            sort = sort,
            order = order
        )
    }

    override fun scrollToTop() {
        viewHolder?.layoutRecyclerView?.recyclerView?.scrollToPosition(0)
    }
}
