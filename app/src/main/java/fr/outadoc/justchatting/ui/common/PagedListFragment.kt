package fr.outadoc.justchatting.ui.common

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import fr.outadoc.justchatting.databinding.CommonRecyclerViewLayoutBinding
import fr.outadoc.justchatting.repository.LoadingState

abstract class PagedListFragment<T : Any, VM : PagedListViewModel<T>, Adapter : BasePagedListAdapter<T>> :
    Fragment() {

    protected abstract val viewModel: VM
    protected abstract val adapter: Adapter

    protected abstract val commonViewHolder: CommonRecyclerViewLayoutBinding?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                adapter.unregisterAdapterDataObserver(this)
                adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        try {
                            if (positionStart == 0) {
                                commonViewHolder?.recyclerView?.scrollToPosition(0)
                            }
                        } catch (e: Exception) {
                        }
                    }
                })
            }
        })

        commonViewHolder?.recyclerView?.adapter = adapter

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars())

            commonViewHolder?.apply {
                recyclerView.setPadding(
                    view.paddingLeft,
                    view.paddingTop,
                    view.paddingRight,
                    insets.bottom
                )
            }

            windowInsets
        }

        viewModel.list.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            commonViewHolder?.nothingHere?.isVisible = it.isEmpty()
        }

        viewModel.loadingState.observe(viewLifecycleOwner) {
            commonViewHolder?.apply {
                val isLoading = it == LoadingState.LOADING
                val isListEmpty = adapter.currentList.isNullOrEmpty()

                if (isLoading) {
                    nothingHere.isVisible = false
                }

                progressBar.isVisible = isLoading && isListEmpty

                if (swipeRefresh.isEnabled) {
                    swipeRefresh.isRefreshing = isLoading && !isListEmpty
                }
            }
        }

        viewModel.pagingState.observe(viewLifecycleOwner, Observer(adapter::setPagingState))

        commonViewHolder?.apply {
            if (swipeRefresh.isEnabled) {
                swipeRefresh.setOnRefreshListener { viewModel.refresh() }
            }
        }
    }
}
