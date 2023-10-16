package fr.outadoc.justchatting.utils.http

import io.ktor.client.HttpClient

expect class HttpClientProvider {
    fun getBaseClient(): HttpClient
    fun getTwitchClient(): HttpClient
}
