package fr.outadoc.justchatting.utils.presentation

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

internal actual abstract class ViewModel {

    actual val viewModelScope = MainScope()

    protected actual open fun onCleared() {}

    fun clear() {
        onCleared()
        viewModelScope.cancel()
    }
}
