package fr.outadoc.justchatting.feature.pronouns.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.path

class AlejoPronounsApi(
    httpClient: HttpClient,
) {
    private val client = httpClient.config {
        defaultRequest {
            url("https://pronouns.alejo.io/api/")
        }
    }

    suspend fun getPronouns(): List<AlejoPronoun> {
        return client.get { url { path("pronouns") } }.body()
    }

    suspend fun getPronounsForUser(login: String): List<UserPronounResponse> {
        return client.get { url { path("users", login) } }.body()
    }
}
