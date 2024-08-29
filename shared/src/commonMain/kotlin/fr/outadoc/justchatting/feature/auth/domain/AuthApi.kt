package fr.outadoc.justchatting.feature.auth.domain

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.auth.domain.model.AuthValidationResponse
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials

internal interface AuthApi {
    suspend fun validateToken(token: String): Result<AuthValidationResponse>
    suspend fun revokeToken(clientId: String, token: String): Result<Unit>
    fun getExternalAuthorizeUrl(oAuthAppCredentials: OAuthAppCredentials, scopes: Set<String>): Uri
}
