package fr.outadoc.justchatting.utils.core

import dev.jordond.connectivity.Connectivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
internal class ConnectivityNetworkStateObserverTest {

    /**
     * A stand-in for the platform [Connectivity] implementation, which on Android backs
     * [statusUpdates] with `ConnectivityManager.registerDefaultNetworkCallback`. That callback
     * fires repeatedly for the same network - e.g. on capability re-validation or when the
     * metered flag changes - not just on real up/down transitions, so [statusUpdates] can emit
     * several non-identical [Connectivity.Status.Connected] values in a row while the device
     * never actually lost connectivity.
     */
    private class FakeConnectivity : Connectivity {
        val statusFlow: MutableSharedFlow<Connectivity.Status> = MutableSharedFlow()
        override val statusUpdates: SharedFlow<Connectivity.Status> = statusFlow
        override val monitoring: StateFlow<Boolean> = MutableStateFlow(true)

        override suspend fun status(): Connectivity.Status = Connectivity.Status.Disconnected
        override fun start() = Unit
        override fun stop() = Unit
    }

    @Test
    fun `repeated Connected updates for the same network collapse into a single Available emission`() = runTest(UnconfinedTestDispatcher()) {
        val connectivity = FakeConnectivity()
        val observer = ConnectivityNetworkStateObserver(connectivity)

        val collected = mutableListOf<NetworkStateObserver.NetworkState>()
        backgroundScope.launch { observer.state.collect { collected.add(it) } }

        connectivity.statusFlow.emit(Connectivity.Status.Connected(metered = false))
        connectivity.statusFlow.emit(Connectivity.Status.Connected(metered = true))
        connectivity.statusFlow.emit(Connectivity.Status.Connected(metered = false))

        assertEquals(
            listOf<NetworkStateObserver.NetworkState>(NetworkStateObserver.NetworkState.Available),
            collected,
        )
    }

    @Test
    fun `real transitions between connected and disconnected are still reported`() = runTest(UnconfinedTestDispatcher()) {
        val connectivity = FakeConnectivity()
        val observer = ConnectivityNetworkStateObserver(connectivity)

        val collected = mutableListOf<NetworkStateObserver.NetworkState>()
        backgroundScope.launch { observer.state.collect { collected.add(it) } }

        connectivity.statusFlow.emit(Connectivity.Status.Connected(metered = false))
        connectivity.statusFlow.emit(Connectivity.Status.Disconnected)
        connectivity.statusFlow.emit(Connectivity.Status.Connected(metered = false))

        assertEquals(
            listOf<NetworkStateObserver.NetworkState>(
                NetworkStateObserver.NetworkState.Available,
                NetworkStateObserver.NetworkState.Unavailable,
                NetworkStateObserver.NetworkState.Available,
            ),
            collected,
        )
    }
}
