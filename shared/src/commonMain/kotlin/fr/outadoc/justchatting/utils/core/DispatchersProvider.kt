package fr.outadoc.justchatting.utils.core

import kotlinx.coroutines.CoroutineDispatcher

internal expect object DispatchersProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}
