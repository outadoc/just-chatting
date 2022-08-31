package fr.outadoc.justchatting.ui.common

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import fr.outadoc.justchatting.repository.LoadingState
import kotlinx.android.synthetic.main.common_recycler_view_layout.nothingHere
import kotlinx.android.synthetic.main.common_recycler_view_layout.progressBar
import kotlinx.android.synthetic.main.common_recycler_view_layout.recyclerView
import kotlinx.android.synthetic.main.common_recycler_view_layout.swipeRefresh

abstract class PagedListFragment<T, VM : PagedListViewModel<T>, Adapter : BasePagedListAdapter<T>> :
    Fragment() {

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

        recyclerView.adapter = adapter

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())

            recyclerView.setPadding(
                view.paddingLeft,
                view.paddingTop,
                view.paddingRight,
                insets.bottom
            )

            windowInsets
        }

        viewModel.list.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            nothingHere?.isVisible = it.isEmpty()
        }

        viewModel.loadingState.observe(viewLifecycleOwner) {
            val isLoading = it == LoadingState.LOADING
            val isListEmpty = adapter.currentList.isNullOrEmpty()
            if (isLoading) {
                nothingHere?.isVisible = false
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
}
