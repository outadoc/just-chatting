package fr.outadoc.justchatting.repository.datasource

import android.util.Log
import androidx.paging.PositionalDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

abstract class BasePositionalDataSource<T : Any>(
    private val coroutineScope: CoroutineScope,
) : PositionalDataSource<T>() {

    private val tag: String = javaClass.simpleName

    protected fun loadInitial(
        params: LoadInitialParams,
        callback: LoadInitialCallback<T>,
        request: suspend () -> List<T>
    ) {
        runBlocking {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    Log.d(tag, "Loading data. Size: " + params.requestedLoadSize)
                    val data = request()
                    callback.onResult(data, 0, data.size)
                    Log.d(tag, "Successfully loaded data")
                } catch (e: Exception) {
                    Log.e(tag, "Error loading data", e)
                }
            }.join()
        }
    }

    protected fun loadRange(
        params: LoadRangeParams,
        callback: LoadRangeCallback<T>,
        request: suspend () -> List<T>
    ) {
        runBlocking {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    Log.d(
                        tag,
                        "Loading data. Size: " + params.loadSize + " offset " + params.startPosition
                    )
                    val data = request()
                    callback.onResult(data)
                    Log.d(tag, "Successfully loaded data")
                } catch (e: Exception) {
                    Log.e(tag, "Error loading data", e)
                }
            }.join()
        }
    }
}
