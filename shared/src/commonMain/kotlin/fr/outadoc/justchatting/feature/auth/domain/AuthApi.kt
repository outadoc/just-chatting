package fr.outadoc.justchatting.feature.auth.domain

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.auth.domain.model.AuthValidationResponse
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials

public interface AuthApi {
    public suspend fun validateToken(token: String): Result<AuthValidationResponse>

    public suspend fun revokeToken(
        clientId: String,
        token: String,
    ): Result<Unit>

    public fun getExternalAuthorizeUrl(
        oAuthAppCredentials: OAuthAppCredentials,
        scopes: Set<String>,
    ): Uri
}
