package fr.outadoc.justchatting.utils.core

import kotlinx.coroutines.CoroutineDispatcher

internal actual object DispatchersProvider {
    actual val main: CoroutineDispatcher
        get() = TODO("Not yet implemented")
    actual val io: CoroutineDispatcher
        get() = TODO("Not yet implemented")
    actual val default: CoroutineDispatcher
        get() = TODO("Not yet implemented")
    actual val unconfined: CoroutineDispatcher
        get() = TODO("Not yet implemented")
}
