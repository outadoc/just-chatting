package fr.outadoc.justchatting.feature.preferences.presentation

internal class NoopLogRepository : LogRepository {

    override val isSupported: Boolean = false

    override suspend fun dumpLogs(): String = ""
}
