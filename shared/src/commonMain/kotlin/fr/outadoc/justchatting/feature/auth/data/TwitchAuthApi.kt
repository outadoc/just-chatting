package fr.outadoc.justchatting.feature.auth.data

import fr.outadoc.justchatting.feature.auth.data.model.TwitchAuthValidationResponse
import fr.outadoc.justchatting.feature.auth.domain.AuthApi
import fr.outadoc.justchatting.feature.auth.domain.model.AuthValidationResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.path

internal class TwitchAuthApi(httpClient: HttpClient) : AuthApi {

    private val client = httpClient.config {
        defaultRequest {
            url("https://id.twitch.tv/oauth2/")
        }
    }

    override suspend fun validateToken(token: String): Result<AuthValidationResponse> =
        runCatching {
            client
                .get {
                    url { path("validate") }
                    headers { append("Authorization", "Bearer $token") }
                }
                .body<TwitchAuthValidationResponse>()
        }.map { response ->
            AuthValidationResponse(
                clientId = response.clientId,
                login = response.login,
                userId = response.userId,
            )
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
