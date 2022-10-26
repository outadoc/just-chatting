package fr.outadoc.justchatting.ui.common

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

abstract class PagedListViewModel<T : Any> : ViewModel() {
    abstract val pagingData: Flow<PagingData<T>>
}
