package fr.outadoc.justchatting.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import fr.outadoc.justchatting.repository.datasource.BaseDataSourceFactory
import fr.outadoc.justchatting.repository.datasource.PagingDataSource

class Listing<T : Any> internal constructor(
    val pagedList: LiveData<PagedList<T>>,
    val loadingState: LiveData<LoadingState>,
    val pagingState: LiveData<LoadingState>,
    val refresh: () -> Unit,
    val retry: () -> Unit
) {

    companion object {

        fun <ListValue : Any, DS> create(
            factory: BaseDataSourceFactory<*, ListValue, DS>,
            config: PagedList.Config
        ): Listing<ListValue> where DS : DataSource<*, ListValue>, DS : PagingDataSource {
            val pagedList = LivePagedListBuilder(factory, config).build()
            val loadingState = factory.sourceLiveData.switchMap { it.loadingState }
            val pagingState = factory.sourceLiveData.switchMap { it.pagingState }
            return Listing(
                pagedList,
                loadingState,
                pagingState,
                refresh = { factory.sourceLiveData.value?.invalidate() },
                retry = { factory.sourceLiveData.value?.retry() }
            )
        }
    }
}
