package fr.outadoc.justchatting.utils.http

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

public interface BaseHttpClientProvider {
    public fun get(block: HttpClientConfig<*>.() -> Unit = {}): HttpClient
}
