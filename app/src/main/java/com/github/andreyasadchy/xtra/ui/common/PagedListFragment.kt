package com.github.andreyasadchy.xtra.ui.common

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.github.andreyasadchy.xtra.repository.LoadingState
import com.github.andreyasadchy.xtra.ui.follow.FollowMediaFragment
import com.github.andreyasadchy.xtra.ui.search.SearchFragment
import com.github.andreyasadchy.xtra.ui.top.TopFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.prefs
import kotlinx.android.synthetic.main.common_recycler_view_layout.*

abstract class PagedListFragment<T, VM : PagedListViewModel<T>, Adapter : BasePagedListAdapter<T>> : BaseNetworkFragment() {

    protected abstract val viewModel: VM
    protected abstract val adapter: Adapter

    private var isTouched = false

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
        if (!requireContext().prefs().getBoolean(C.UI_SCROLLTOP, true) || parentFragment is TopFragment || parentFragment is FollowMediaFragment || parentFragment is SearchFragment) {
            scrollTop.isEnabled = false
        }
        recyclerView.let {
            it.adapter = adapter
            if (scrollTop.isEnabled) {
                it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        isTouched = newState == RecyclerView.SCROLL_STATE_DRAGGING
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
        viewModel.list.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            nothingHere.isVisible = it.isEmpty()
        })
        viewModel.loadingState.observe(viewLifecycleOwner, Observer {
            val isLoading = it == LoadingState.LOADING
            val isListEmpty = adapter.currentList.isNullOrEmpty()
            if (isLoading) {
                nothingHere.gone()
            }
            progressBar.isVisible = isLoading && isListEmpty
            if (swipeRefresh.isEnabled) {
                swipeRefresh.isRefreshing = isLoading && !isListEmpty
            }
        })
        viewModel.pagingState.observe(viewLifecycleOwner, Observer(adapter::setPagingState))
        if (swipeRefresh.isEnabled) {
            swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        }
        if (scrollTop.isEnabled) {
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