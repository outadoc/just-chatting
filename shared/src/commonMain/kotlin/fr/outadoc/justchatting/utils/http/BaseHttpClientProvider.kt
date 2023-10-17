package fr.outadoc.justchatting.utils.http

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

interface BaseHttpClientProvider {
    fun get(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient
}
