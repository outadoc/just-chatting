package fr.outadoc.justchatting.utils.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class NoopNetworkStateObserver : NetworkStateObserver {
    override val state: Flow<NetworkStateObserver.NetworkState> = flowOf(
        NetworkStateObserver.NetworkState.Available,
    )
}
