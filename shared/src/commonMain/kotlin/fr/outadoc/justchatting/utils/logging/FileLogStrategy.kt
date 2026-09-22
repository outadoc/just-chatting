package fr.outadoc.justchatting.utils.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import okio.FileSystem
import okio.IOException
import okio.Path
import okio.buffer
import okio.use
import kotlin.time.Clock

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
    private val clock: Clock = Clock.System,
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
        // Stamped here, not in the write loop: that batches, so lines reach the sink late.
        pendingLines.trySend("${now()} [${level.tag}] $tag: $content\n")
    }

    /** Local wall-clock time as `HH:mm:ss.SSS`; no date, since the file holds one session. */
    private fun now(): String {
        val time =
            clock
                .now()
                .toLocalDateTime(TimeZone.currentSystemDefault())
                .time

        return buildString {
            append(time.hour.toString().padStart(2, '0'))
            append(':')
            append(time.minute.toString().padStart(2, '0'))
            append(':')
            append(time.second.toString().padStart(2, '0'))
            append('.')
            append((time.nanosecond / 1_000_000).toString().padStart(3, '0'))
        }
    }

    private suspend fun writeLoop() {
        try {
            logFilePath.parent?.let { parent ->
                fileSystem.createDirectories(
                    parent,
                    mustCreate = false,
                )
            }

            // Not appendingSink: each new instance (i.e. each app launch) should start from
            // a clean file rather than keep appending to whatever was left over.
            fileSystem.sink(logFilePath, mustCreate = false).buffer().use { sink ->
                for (line in pendingLines) {
                    sink.writeUtf8(line)

                    // Batch the flush: drain everything already queued before touching disk
                    // instead of flushing (a syscall) after every single line. Bursty callers
                    // (e.g. one log line per chat message) end up costing one flush per burst
                    // instead of one per line, while an idle logger still flushes promptly
                    // since draining stops as soon as the channel has nothing buffered.
                    var next = pendingLines.tryReceive().getOrNull()
                    while (next != null) {
                        sink.writeUtf8(next)
                        next = pendingLines.tryReceive().getOrNull()
                    }

                    sink.flush()
                }
            }
        } catch (e: IOException) {
            // Logging must never crash the app it's meant to help debug.
        }
    }
}
