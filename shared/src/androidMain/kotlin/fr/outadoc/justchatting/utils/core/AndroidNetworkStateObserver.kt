package fr.outadoc.justchatting.utils.core

import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import fr.outadoc.justchatting.utils.core.NetworkStateObserver.NetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

internal class AndroidNetworkStateObserver(
    private val connectivityManager: ConnectivityManager,
) : NetworkStateObserver {

    override val state: Flow<NetworkState> = callbackFlow {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkState.Available)
            }

            override fun onLost(network: Network) {
                trySend(NetworkState.Unavailable)
            }

            override fun onUnavailable() {
                trySend(NetworkState.Unavailable)
            }
        }

        connectivityManager.registerNetworkCallback(request, callback)

        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }
}
