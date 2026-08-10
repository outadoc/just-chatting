package fr.outadoc.justchatting.feature.preferences.presentation

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.http.toUri
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.buffer
import okio.gzip
import okio.use
import kotlin.time.Clock

/**
 * Reads back whatever [fr.outadoc.justchatting.utils.logging.FileLogStrategy] wrote to
 * [logFilePath], gzips it into [dumpDirectory], and exposes it as a shareable [Uri].
 *
 * Platforms that need a different [Uri] scheme (e.g. Android's content:// requirement for
 * sharing files with other apps) can override [toShareableUri].
 */
internal open class FileLogRepository(
    private val logFilePath: Path,
    private val dumpDirectory: Path,
    private val fileSystem: FileSystem,
    private val dispatchersProvider: DispatchersProvider,
    private val clock: Clock,
) : LogRepository {
    override val isSupported: Boolean = true

    override suspend fun dumpLogs(): Uri = withContext(dispatchersProvider.io) {
        toShareableUri(gzipLogFile())
    }

    protected open fun toShareableUri(path: Path): Uri = "file://$path".toUri()

    private fun gzipLogFile(): Path {
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
}
