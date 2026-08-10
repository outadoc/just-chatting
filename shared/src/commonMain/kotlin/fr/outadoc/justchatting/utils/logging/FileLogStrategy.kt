package fr.outadoc.justchatting.utils.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.buffer
import okio.use

/**
 * Persists every log line to [logFilePath] using [fileSystem], so it can later be read back
 * by [fr.outadoc.justchatting.feature.preferences.presentation.LogRepository] regardless of
 * platform.
 *
 * [println] can be called extremely often (e.g. once per chat message received), so it never
 * touches the filesystem itself: it just hands the formatted line off to a channel, and a
 * single background coroutine keeps one sink open and drains it sequentially. This keeps the
 * hot path allocation-only and avoids reopening the file for every line.
 *
 * [logFilePath] is truncated once, the first time an instance is constructed (i.e. once per
 * app launch, since this is a Koin singleton), so the log file only ever holds the current
 * session instead of growing forever across launches.
 */
internal class FileLogStrategy(
    private val logFilePath: Path,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
) : LogStrategy {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val pendingLines = Channel<String>(capacity = Channel.UNLIMITED)

    init {
        scope.launch { writeLoop() }
    }

    override fun println(
        level: Logger.Level,
        tag: String?,
        content: String,
    ) {
        pendingLines.trySend("[${level.tag}] $tag: $content\n")
    }

    private suspend fun writeLoop() {
        try {
            logFilePath.parent?.let { parent -> fileSystem.createDirectories(parent, mustCreate = false) }

            // Not appendingSink: each new instance (i.e. each app launch) should start from
            // a clean file rather than keep appending to whatever was left over.
            fileSystem.sink(logFilePath, mustCreate = false).buffer().use { sink ->
                for (line in pendingLines) {
                    sink.writeUtf8(line)
                    sink.flush()
                }
            }
        } catch (e: IOException) {
            // Logging must never crash the app it's meant to help debug.
        }
    }
}
