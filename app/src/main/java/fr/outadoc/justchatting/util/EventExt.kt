package fr.outadoc.justchatting.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <T : Any> LiveData<Event<T>>.observeEvent(owner: LifecycleOwner, observer: ((T) -> Unit)) {
    observe(owner) { event ->
        event.getContentIfNotHandled()?.let { observer(it) }
    }
}

fun <T : Any> Flow<T>.asEventLiveData(): LiveData<Event<T>> = map { Event(it) }.asLiveData()
