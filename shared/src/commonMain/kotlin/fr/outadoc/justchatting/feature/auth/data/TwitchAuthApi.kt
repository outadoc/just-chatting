package fr.outadoc.justchatting.feature.auth.data

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.auth.data.model.TwitchAuthValidationResponse
import fr.outadoc.justchatting.feature.auth.domain.AuthApi
import fr.outadoc.justchatting.feature.auth.domain.model.AuthValidationResponse
import fr.outadoc.justchatting.feature.auth.domain.model.InvalidTokenException
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.preferences.domain.model.ApiToken
import fr.outadoc.justchatting.feature.shared.data.ApiEndpoints
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import kotlinx.collections.immutable.toImmutableSet

internal class TwitchAuthApi(
    httpClient: HttpClient,
) : AuthApi {
    private val client =
        httpClient.config {
            defaultRequest {
                url(ApiEndpoints.TWITCH_AUTH_BASE)
            }
        }

    override suspend fun validateToken(token: ApiToken): Result<AuthValidationResponse> =
        runCatching {
            try {
                client
                    .get {
                        url { path("validate") }
                        headers { append("Authorization", "Bearer ${token.value}") }
                    }.body<TwitchAuthValidationResponse>()
            } catch (e: ClientRequestException) {
                // Twitch answers 401 when the token is invalid or expired.
                // Anything else (network failure, timeout, server error) is considered transient.
                if (e.response.status == HttpStatusCode.Unauthorized) {
                    throw InvalidTokenException(cause = e)
                }
                throw e
            }
        }.map { response ->
            AuthValidationResponse(
                clientId = response.clientId,
                login = response.login,
                userId = response.userId,
                scopes = response.scopes.toImmutableSet(),
            )
        }

    override suspend fun revokeToken(
        clientId: String,
        token: ApiToken,
    ): Result<Unit> =
        runCatching {
            client.post {
                url {
                    path("revoke")
                    parameter("client_id", clientId)
                    parameter("token", token.value)
                }
            }
        }

    override fun getExternalAuthorizeUrl(
        oAuthAppCredentials: OAuthAppCredentials,
        scopes: Set<String>,
    ): Uri =
        Uri
            .parse(ApiEndpoints.TWITCH_AUTH_AUTHORIZE)
            .buildUpon()
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("client_id", oAuthAppCredentials.clientId)
            .appendQueryParameter("redirect_uri", oAuthAppCredentials.redirectUri)
            .appendQueryParameter("force_verify", "true")
            .appendQueryParameter("scope", scopes.joinToString(" "))
            .build()
}
