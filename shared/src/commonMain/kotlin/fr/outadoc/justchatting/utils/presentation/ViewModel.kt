package fr.outadoc.justchatting.utils.presentation

import kotlinx.coroutines.CoroutineScope

/**
 * https://proandroiddev.com/multiplatform-presenters-or-viewmodels-the-lean-way-cbb763c803af
 */
internal expect abstract class ViewModel() {
    val viewModelScope: CoroutineScope
    protected open fun onCleared()
}
