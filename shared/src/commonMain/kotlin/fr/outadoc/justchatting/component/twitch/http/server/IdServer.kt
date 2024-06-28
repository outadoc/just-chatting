package fr.outadoc.justchatting.component.twitch.http.server

import fr.outadoc.justchatting.component.twitch.http.api.IdApi
import fr.outadoc.justchatting.component.twitch.http.model.ValidationResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.path

internal class IdServer(httpClient: HttpClient) : IdApi {

    private val client = httpClient.config {
        defaultRequest {
            url("https://id.twitch.tv/oauth2/")
        }
    }

    override suspend fun validateToken(token: String): Result<ValidationResponse> =
        runCatching {
            client.get {
                url { path("validate") }
                headers { append("Authorization", "Bearer $token") }
            }.body()
        }

    override suspend fun revokeToken(clientId: String, token: String): Result<Unit> {
        return runCatching {
            client.post {
                url {
                    path("revoke")
                    parameter("client_id", clientId)
                    parameter("token", token)
                }
            }
        }
    }
}
