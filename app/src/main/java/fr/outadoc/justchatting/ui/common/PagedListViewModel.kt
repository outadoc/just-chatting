package fr.outadoc.justchatting.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

abstract class PagedListViewModel<T : Any> : ViewModel() {

    protected abstract val result: Flow<Pager<*, T>>

    val pagingData: Flow<PagingData<T>>
        get() = result.flatMapLatest { pager -> pager.flow }
            .cachedIn(viewModelScope)
}
