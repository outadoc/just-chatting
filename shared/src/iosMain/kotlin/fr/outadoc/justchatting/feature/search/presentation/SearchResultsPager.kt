package fr.outadoc.justchatting.feature.search.presentation

import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.CombinedLoadStates
import androidx.paging.PagingData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import kotlinx.coroutines.flow.Flow

public class SearchResultsPager {
    private val differ =
        AsyncPagingDataDiffer(
            diffCallback =
                object : DiffUtil.ItemCallback<ChannelSearchResult>() {
                    override fun areItemsTheSame(
                        oldItem: ChannelSearchResult,
                        newItem: ChannelSearchResult,
                    ): Boolean = oldItem == newItem

                    override fun areContentsTheSame(
                        oldItem: ChannelSearchResult,
                        newItem: ChannelSearchResult,
                    ): Boolean = oldItem == newItem
                },
            updateCallback =
                object : ListUpdateCallback {
                    override fun onInserted(
                        position: Int,
                        count: Int,
                    ) = Unit

                    override fun onRemoved(
                        position: Int,
                        count: Int,
                    ) = Unit

                    override fun onMoved(
                        fromPosition: Int,
                        toPosition: Int,
                    ) = Unit

                    override fun onChanged(
                        position: Int,
                        count: Int,
                        payload: Any?,
                    ) = Unit
                },
        )

    public val loadStateFlow: Flow<CombinedLoadStates> = differ.loadStateFlow
    public val onPagesUpdatedFlow: Flow<Unit> = differ.onPagesUpdatedFlow

    public fun snapshot(): List<ChannelSearchResult> = differ.snapshot().items.filterNotNull()

    public fun getItem(index: Int): ChannelSearchResult? = differ.getItem(index)

    public suspend fun submitData(pagingData: PagingData<ChannelSearchResult>) {
        differ.submitData(pagingData)
    }
}
