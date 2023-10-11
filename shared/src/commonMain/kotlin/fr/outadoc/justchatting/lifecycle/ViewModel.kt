package fr.outadoc.justchatting.lifecycle

import kotlinx.coroutines.CoroutineScope

/**
 * https://proandroiddev.com/multiplatform-presenters-or-viewmodels-the-lean-way-cbb763c803af
 */
expect abstract class ViewModel() {
    val viewModelScope: CoroutineScope
    protected open fun onCleared()
}
