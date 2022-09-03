package fr.outadoc.justchatting.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

fun <T : Any> LiveData<Event<T>>.observeEvent(owner: LifecycleOwner, observer: ((T) -> Unit)) {
    observe(owner) { event ->
        event.getContentIfNotHandled()?.let { observer(it) }
    }
}
