package fr.outadoc.justchatting.feature.preferences.presentation

import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.gzip
import okio.use
import kotlin.time.Clock

/**
 * Reads back logs written by [fr.outadoc.justchatting.utils.logging.FileLogStrategy].
 * Override [exportLogs] and reuse [gzipLogFile] for platforms that need a file handoff
 * instead of the clipboard default.
 */
internal open class FileLogRepository(
    private val logFilePath: Path,
    private val dumpDirectory: Path,
    private val fileSystem: FileSystem,
    private val dispatchersProvider: DispatchersProvider,
    private val clock: Clock,
) : LogRepository {
    override val isSupported: Boolean = true

    override suspend fun exportLogs(): LogExportResult = withContext(dispatchersProvider.io) {
        LogExportResult.CopyToClipboard(readTailOfLogFile())
    }

    protected fun gzipLogFile(): Path {
        fileSystem.createDirectories(dumpDirectory, mustCreate = false)

        val outPath = dumpDirectory / "${clock.now().toEpochMilliseconds()}.log.gz"

        fileSystem.sink(outPath).gzip().buffer().use { sink ->
            if (fileSystem.exists(logFilePath)) {
                fileSystem.source(logFilePath).buffer().use { source ->
                    source.readAll(sink)
                }
            }
        }

        return outPath
    }

    private fun readTailOfLogFile(): String {
        if (!fileSystem.exists(logFilePath)) return ""

        val size = fileSystem.metadata(logFilePath).size ?: 0L

        return fileSystem.source(logFilePath).buffer().use { source ->
            if (size > MAX_CLIPBOARD_BYTES) {
                source.skip(size - MAX_CLIPBOARD_BYTES)
                // The skip almost certainly landed mid-line; drop that partial line.
                source.readUtf8Line()
            }
            source.readUtf8()
        }
    }

    private companion object {
        const val MAX_CLIPBOARD_BYTES = 2_000_000L
    }
}
