package fr.outadoc.justchatting.feature.pronouns.data

import fr.outadoc.justchatting.feature.pronouns.data.model.AlejoPronoun
import fr.outadoc.justchatting.feature.pronouns.data.model.UserPronounResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url

internal class AlejoPronounsClient(
    httpClient: HttpClient,
) {
    private val client = httpClient

    suspend fun getPronouns(): Result<Map<String, AlejoPronoun>> {
        return runCatching {
            client
                .get { url("https://api.pronouns.alejo.io/v1/pronouns") }
                .body()
        }
    }

    suspend fun getPronounsForUser(login: String): Result<List<UserPronounResponse>> {
        return runCatching {
            client
                .get { url("https://pronouns.alejo.io/api/users/$login") }
                .body()
        }
    }
}
