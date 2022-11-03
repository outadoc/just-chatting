package fr.outadoc.justchatting.ui.common

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import fr.outadoc.justchatting.databinding.CommonRecyclerViewLayoutBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class PagedListFragment<T : Any, VM : PagedListViewModel<T>, Adapter : BasePagedListAdapter<T>> :
    Fragment() {

    protected abstract val viewModel: VM
    protected abstract val adapter: Adapter

    protected abstract val commonViewHolder: CommonRecyclerViewLayoutBinding?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        commonViewHolder?.apply {
            recyclerView.adapter = adapter
            swipeRefresh.setOnRefreshListener { adapter.refresh() }
        }

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

        lifecycleScope.launch {
            viewModel.pagingData.collectLatest { data ->
                adapter.submitData(data)
            }
        }

        lifecycleScope.launch {
            adapter.loadStateFlow.collectLatest { loadStates ->
                commonViewHolder?.apply {
                    swipeRefresh.isRefreshing = loadStates.refresh is LoadState.Loading
                    nothingHere.isVisible =
                        loadStates.source.refresh is LoadState.NotLoading &&
                                loadStates.append.endOfPaginationReached &&
                                adapter.itemCount < 1
                }
            }
        }
    }
}
