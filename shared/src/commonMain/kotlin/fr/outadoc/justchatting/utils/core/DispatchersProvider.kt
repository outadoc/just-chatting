package fr.outadoc.justchatting.utils.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

public object DispatchersProvider {
    public val main: CoroutineDispatcher = Dispatchers.Main
    public val io: CoroutineDispatcher = Dispatchers.IO
    public val default: CoroutineDispatcher = Dispatchers.Default
    public val unconfined: CoroutineDispatcher = Dispatchers.Unconfined
}
