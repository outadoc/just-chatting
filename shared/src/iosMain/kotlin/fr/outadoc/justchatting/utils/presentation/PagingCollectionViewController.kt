package fr.outadoc.justchatting.utils.presentation

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.CombinedLoadStates
import androidx.paging.ItemSnapshotList
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Making abstract causes the compilation error "Non-final Kotlin subclasses of Objective-C classes are not yet supported".
internal class PagingCollectionViewController<T : Any> {

    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.Default

    private val diffCallback = object : DiffUtil.ItemCallback<T>() {
        override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
            return oldItem == newItem
        }
    }

    private val differ = AsyncPagingDataDiffer(
        diffCallback,
        object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
            }

            override fun onRemoved(position: Int, count: Int) {
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
            }
        },
        mainDispatcher = mainDispatcher,
        workerDispatcher = workerDispatcher,
    )

    suspend fun submitData(pagingData: PagingData<T>) {
        differ.submitData(pagingData)
    }

    fun retry() {
        differ.retry()
    }

    fun refresh() {
        differ.refresh()
    }

    fun getItem(position: Int) = differ.getItem(position)

    fun peek(index: Int) = differ.peek(index)

    fun snapshot(): ItemSnapshotList<T> = differ.snapshot()

    val loadStateFlow: Flow<CombinedLoadStates> = differ.loadStateFlow

    val onPagesUpdatedFlow: Flow<Unit> = differ.onPagesUpdatedFlow

    val itemsFlow: Flow<List<T>>
        get() = differ.onPagesUpdatedFlow.map { differ.snapshot().items }
}
