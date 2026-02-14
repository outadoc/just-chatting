package fr.outadoc.justchatting.feature.preferences.presentation

import com.eygraber.uri.Uri

internal interface LogRepository {
    val isSupported: Boolean

    suspend fun dumpLogs(): Uri
}
