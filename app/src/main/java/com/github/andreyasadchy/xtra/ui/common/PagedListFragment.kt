package com.github.andreyasadchy.xtra.ui.common

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.github.andreyasadchy.xtra.repository.LoadingState
import com.github.andreyasadchy.xtra.ui.follow.FollowMediaFragment
import com.github.andreyasadchy.xtra.ui.follow.FollowPagerFragment
import com.github.andreyasadchy.xtra.ui.search.SearchFragment
import com.github.andreyasadchy.xtra.util.gone
import kotlinx.android.synthetic.main.common_recycler_view_layout.nothingHere
import kotlinx.android.synthetic.main.common_recycler_view_layout.progressBar
import kotlinx.android.synthetic.main.common_recycler_view_layout.recyclerView
import kotlinx.android.synthetic.main.common_recycler_view_layout.scrollTop
import kotlinx.android.synthetic.main.common_recycler_view_layout.swipeRefresh

abstract class PagedListFragment<T, VM : PagedListViewModel<T>, Adapter : BasePagedListAdapter<T>> :
    BaseNetworkFragment() {

    protected abstract val viewModel: VM
    protected abstract val adapter: Adapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                adapter.unregisterAdapterDataObserver(this)
                adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        try {
                            if (positionStart == 0) {
                                recyclerView?.scrollToPosition(0)
                            }
                        } catch (e: Exception) {
                        }
                    }
                })
            }
        })
        if (parentFragment is FollowMediaFragment || parentFragment is FollowPagerFragment || parentFragment is SearchFragment) {
            scrollTop.isEnabled = false
        }
        recyclerView.let {
            it.adapter = adapter
            if (scrollTop?.isEnabled == true) {
                it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        scrollTop.isVisible = shouldShowButton()
                    }
                })
            }
        }
    }

    private fun shouldShowButton(): Boolean {
        val offset = recyclerView.computeVerticalScrollOffset()
        if (offset < 0) {
            return false
        }
        val extent = recyclerView.computeVerticalScrollExtent()
        val range = recyclerView.computeVerticalScrollRange()
        val percentage = (100f * offset / (range - extent).toFloat())
        return percentage > 3f
    }

    override fun initialize() {
        viewModel.list.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            nothingHere?.isVisible = it.isEmpty()
        }
        viewModel.loadingState.observe(viewLifecycleOwner) {
            val isLoading = it == LoadingState.LOADING
            val isListEmpty = adapter.currentList.isNullOrEmpty()
            if (isLoading) {
                nothingHere?.gone()
            }
            progressBar?.isVisible = isLoading && isListEmpty
            if (swipeRefresh?.isEnabled == true) {
                swipeRefresh.isRefreshing = isLoading && !isListEmpty
            }
        }
        viewModel.pagingState.observe(viewLifecycleOwner, Observer(adapter::setPagingState))
        if (swipeRefresh?.isEnabled == true) {
            swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        }
        if (scrollTop?.isEnabled == true) {
            scrollTop.setOnClickListener {
                (parentFragment as? Scrollable)?.scrollToTop()
                it.gone()
            }
        }
    }

    override fun onNetworkRestored() {
        viewModel.retry()
    }
}
