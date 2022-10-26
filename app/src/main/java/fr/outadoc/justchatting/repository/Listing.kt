package fr.outadoc.justchatting.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import fr.outadoc.justchatting.repository.datasource.BaseDataSourceFactory
import fr.outadoc.justchatting.repository.datasource.PagingDataSource

data class Listing<T : Any>(
    val pagedList: LiveData<PagedList<T>>,
    val loadingState: LiveData<LoadingState>,
    val pagingState: LiveData<LoadingState>,
    val refresh: () -> Unit,
    val retry: () -> Unit
)

fun <ListValue : Any, DS> createListing(
    factory: BaseDataSourceFactory<*, ListValue, DS>,
    config: PagedList.Config
): Listing<ListValue> where DS : DataSource<*, ListValue>, DS : PagingDataSource {
    return Listing(
        pagedList = LivePagedListBuilder(factory, config).build(),
        loadingState = factory.sourceLiveData.switchMap { it.loadingState },
        pagingState = factory.sourceLiveData.switchMap { it.pagingState },
        refresh = { factory.sourceLiveData.value?.invalidate() },
        retry = { factory.sourceLiveData.value?.retry() }
    )
}