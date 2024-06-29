package fr.outadoc.justchatting.utils.presentation

import kotlinx.coroutines.CoroutineScope
import androidx.lifecycle.ViewModel as AndroidXViewModel
import androidx.lifecycle.viewModelScope as androidXViewModelScope

internal actual abstract class ViewModel actual constructor() : AndroidXViewModel() {

    actual val viewModelScope: CoroutineScope = androidXViewModelScope
    actual override fun onCleared() = super.onCleared()
}
