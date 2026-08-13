package fr.outadoc.justchatting.feature.preferences.presentation

import android.content.Context
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import kotlin.time.Clock

internal class AndroidLogRepository(
    private val applicationContext: Context,
    logFilePath: Path,
    dumpDirectory: Path,
    fileSystem: FileSystem,
    private val dispatchersProvider: DispatchersProvider,
    clock: Clock,
) : FileLogRepository(logFilePath, dumpDirectory, fileSystem, dispatchersProvider, clock) {
    override suspend fun exportLogs(): LogExportResult =
        withContext(dispatchersProvider.io) {
            LogExportResult.Share(
                uri = LogFileProvider.getUri(applicationContext, gzipLogFile()),
            )
        }
}
