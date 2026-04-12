package fr.outadoc.justchatting.feature.preferences.presentation

import com.eygraber.uri.Uri

public interface LogRepository {
    public val isSupported: Boolean

    public suspend fun dumpLogs(): Uri
}
