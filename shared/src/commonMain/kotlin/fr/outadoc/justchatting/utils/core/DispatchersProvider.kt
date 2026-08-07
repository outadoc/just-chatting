package fr.outadoc.justchatting.utils.core

import kotlinx.coroutines.CoroutineDispatcher

internal interface DispatchersProvider {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}
