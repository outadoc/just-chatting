package fr.outadoc.justchatting.repository.datasource

import androidx.lifecycle.MutableLiveData
import fr.outadoc.justchatting.repository.LoadingState

interface PagingDataSource {
    val loadingState: MutableLiveData<LoadingState>
    val pagingState: MutableLiveData<LoadingState>
    fun retry()
}
