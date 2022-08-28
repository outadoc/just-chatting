package com.github.andreyasadchy.xtra.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

fun <T, K, R> LiveData<T>.combineWith(
    liveData: LiveData<K>,
    block: (T, K) -> R
): LiveData<R> {
    val result = MediatorLiveData<R>()
    result.addSource(this) { r1 ->
        result.removeSource(this)
        result.addSource(liveData) { r2 ->
            result.value = block(r1, r2)
        }
    }
    return result
}
