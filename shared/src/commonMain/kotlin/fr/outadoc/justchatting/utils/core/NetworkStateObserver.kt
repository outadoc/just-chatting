package fr.outadoc.justchatting.utils.core

import kotlinx.coroutines.flow.Flow

interface NetworkStateObserver {

    val state: Flow<NetworkState>

    sealed class NetworkState {
        data object Available : NetworkState()
        data object Unavailable : NetworkState()
    }
}