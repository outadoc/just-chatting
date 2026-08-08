package fr.outadoc.justchatting.feature.auth.data

import kotlinx.coroutines.flow.SharedFlow

internal interface LocalCallbackWebServer {
    val receivedUris: SharedFlow<String>

    fun start()

    fun stop()
}
