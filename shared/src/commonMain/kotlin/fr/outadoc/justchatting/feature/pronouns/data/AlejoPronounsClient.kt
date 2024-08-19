package fr.outadoc.justchatting.feature.pronouns.data

import fr.outadoc.justchatting.data.ApiEndpoints
import fr.outadoc.justchatting.feature.pronouns.data.model.AlejoPronoun
import fr.outadoc.justchatting.feature.pronouns.data.model.UserPronounResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.http.path

internal class AlejoPronounsClient(
    httpClient: HttpClient,
) {
    private val client = httpClient.config {
        defaultRequest {
            url(ApiEndpoints.ALEJO_PRONOUNS)
        }
    }

    suspend fun getPronouns(): Result<Map<String, AlejoPronoun>> {
        return runCatching {
            client.get { url { path("v1/pronouns") } }.body()
        }
    }

    suspend fun getPronounsForUser(login: String): Result<UserPronounResponse> {
        return runCatching {
            client.get { url { path("v1/user", login) } }.body()
        }
    }
}
