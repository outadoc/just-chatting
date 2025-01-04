package fr.outadoc.justchatting.utils.core

import dev.jordond.connectivity.Connectivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ConnectivityNetworkStateObserver : NetworkStateObserver {

    private val connectivity = Connectivity {
        autoStart = true
    }

    override val state: Flow<NetworkStateObserver.NetworkState>
        get() = connectivity.statusUpdates.map { status ->
            when (status) {
                is Connectivity.Status.Connected -> {
                    NetworkStateObserver.NetworkState.Available
                }

                Connectivity.Status.Disconnected -> {
                    NetworkStateObserver.NetworkState.Unavailable
                }
            }
        }
}
