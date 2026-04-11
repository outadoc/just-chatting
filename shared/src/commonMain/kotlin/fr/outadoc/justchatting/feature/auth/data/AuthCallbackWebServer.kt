package fr.outadoc.justchatting.feature.auth.data

import kotlinx.coroutines.flow.SharedFlow

internal interface AuthCallbackWebServer {
    public val receivedUris: SharedFlow<String>

    public fun start()

    public fun stop()
}
