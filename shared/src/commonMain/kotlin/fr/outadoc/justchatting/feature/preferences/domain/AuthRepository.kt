package fr.outadoc.justchatting.feature.preferences.domain

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import kotlinx.coroutines.flow.Flow

internal interface AuthRepository {
    val currentUser: Flow<AppUser>

    suspend fun saveToken(token: String)

    suspend fun logout()

    fun getExternalAuthorizeUrl(): Uri
}
