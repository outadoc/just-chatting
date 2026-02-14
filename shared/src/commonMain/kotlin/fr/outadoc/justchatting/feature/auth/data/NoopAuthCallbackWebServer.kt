package fr.outadoc.justchatting.feature.auth.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal class NoopAuthCallbackWebServer : AuthCallbackWebServer {
    override val receivedUris: SharedFlow<String> = MutableSharedFlow()

    override fun start() {}

    override fun stop() {}
}
