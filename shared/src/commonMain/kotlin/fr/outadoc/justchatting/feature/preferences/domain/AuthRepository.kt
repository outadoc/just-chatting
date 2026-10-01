package fr.outadoc.justchatting.feature.preferences.domain

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.preferences.domain.model.ApiToken
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.coroutines.flow.Flow

internal interface AuthRepository {
    val currentUser: Flow<AppUser>

    suspend fun saveToken(token: ApiToken)

    suspend fun logout()

    /**
     * Re-validates the current token, e.g. after [AppUser.ValidationFailed] was emitted.
     */
    fun retryValidation()

    fun getExternalAuthorizeUrl(): Uri
}
