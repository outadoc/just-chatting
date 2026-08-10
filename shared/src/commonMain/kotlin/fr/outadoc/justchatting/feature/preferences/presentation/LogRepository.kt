package fr.outadoc.justchatting.feature.preferences.presentation

import com.eygraber.uri.Uri

internal interface LogRepository {
    val isSupported: Boolean

    suspend fun exportLogs(): LogExportResult
}

internal sealed interface LogExportResult {
    data class Share(val uri: Uri) : LogExportResult
    data class CopyToClipboard(val text: String) : LogExportResult
}
