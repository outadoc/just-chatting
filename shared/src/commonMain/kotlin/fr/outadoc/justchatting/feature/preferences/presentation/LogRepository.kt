package fr.outadoc.justchatting.feature.preferences.presentation

import com.eygraber.uri.Uri

interface LogRepository {
    suspend fun dumpLogs(): Uri
}
