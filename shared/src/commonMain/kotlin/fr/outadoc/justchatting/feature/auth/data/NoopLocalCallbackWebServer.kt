package fr.outadoc.justchatting.feature.auth.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal class NoopLocalCallbackWebServer : LocalCallbackWebServer {
    override val receivedUris: SharedFlow<String> = MutableSharedFlow()

    override fun start() {}

    override fun stop() {}
}
