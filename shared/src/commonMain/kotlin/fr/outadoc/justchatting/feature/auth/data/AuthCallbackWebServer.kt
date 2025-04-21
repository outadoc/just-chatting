package fr.outadoc.justchatting.feature.auth.data

import kotlinx.coroutines.flow.SharedFlow

internal interface AuthCallbackWebServer {
    val receivedUris: SharedFlow<String>
    fun start()
    fun stop()
}
