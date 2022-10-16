package fr.outadoc.justchatting.repository.datasource

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource

abstract class BaseDataSourceFactory<Key : Any, Value : Any, DS> :
    DataSource.Factory<Key, Value>() where DS : DataSource<Key, Value>, DS : PagingDataSource {

    val sourceLiveData = MutableLiveData<DS>()
}
