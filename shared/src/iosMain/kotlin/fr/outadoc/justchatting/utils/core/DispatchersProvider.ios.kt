package fr.outadoc.justchatting.utils.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual object DispatchersProvider {
    actual val main: CoroutineDispatcher = Dispatchers.Main
    actual val io: CoroutineDispatcher = Dispatchers.Default
    actual val default: CoroutineDispatcher = Dispatchers.Default
    actual val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
