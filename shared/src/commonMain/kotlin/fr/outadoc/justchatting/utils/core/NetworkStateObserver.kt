package fr.outadoc.justchatting.utils.core

import kotlinx.coroutines.flow.Flow

public interface NetworkStateObserver {
    public val state: Flow<NetworkState>

    public sealed class NetworkState {
        public data object Available : NetworkState()

        public data object Unavailable : NetworkState()
    }
}
