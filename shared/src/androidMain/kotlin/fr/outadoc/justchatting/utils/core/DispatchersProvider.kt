package fr.outadoc.justchatting.utils.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual object DispatchersProvider {
    actual val main: CoroutineDispatcher
        get() = Dispatchers.Main
    actual val io: CoroutineDispatcher
        get() = Dispatchers.IO
    actual val default: CoroutineDispatcher
        get() = Dispatchers.Default
    actual val unconfined: CoroutineDispatcher
        get() = Dispatchers.Unconfined
}
