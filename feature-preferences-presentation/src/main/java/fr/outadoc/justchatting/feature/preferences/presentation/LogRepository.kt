package fr.outadoc.justchatting.feature.preferences.presentation

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Sink
import okio.buffer
import okio.gzip
import okio.sink
import okio.source
import java.util.UUID

class LogRepository(private val applicationContext: Context) {

    private val logsPath: Path
        get() = (applicationContext.cacheDir.toOkioPath() / "logs")
            .also { dir -> dir.toFile().mkdir() }

    private fun getRandomFilePath(): Path {
        val uuid: String = UUID.randomUUID().toString()
        return logsPath / "$uuid.log.gz"
    }

    suspend fun dumpLogs(): Uri = withContext(Dispatchers.IO) {
        val process: Process =
            Runtime.getRuntime().exec("logcat -d")

        val outPath: Path = getRandomFilePath()

        val source: BufferedSource = process.inputStream.source().buffer()
        val sink: Sink = outPath.toFile().sink().gzip()

        source.use {
            sink.use {
                source.readAll(sink)
            }
        }

        LogFileProvider.getUri(applicationContext, outPath)
    }
}