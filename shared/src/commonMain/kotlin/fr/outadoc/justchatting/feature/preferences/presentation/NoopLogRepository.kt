package fr.outadoc.justchatting.feature.preferences.presentation

import com.eygraber.uri.Uri

internal class NoopLogRepository : LogRepository {
    override val isSupported: Boolean = false

    override suspend fun dumpLogs(): Uri = Uri.EMPTY
}
