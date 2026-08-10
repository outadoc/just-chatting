package fr.outadoc.justchatting.feature.preferences.presentation

import android.content.Context
import com.eygraber.uri.Uri
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import okio.FileSystem
import okio.Path
import kotlin.time.Clock

internal class AndroidLogRepository(
    private val applicationContext: Context,
    logFilePath: Path,
    dumpDirectory: Path,
    fileSystem: FileSystem,
    dispatchersProvider: DispatchersProvider,
    clock: Clock,
) : FileLogRepository(logFilePath, dumpDirectory, fileSystem, dispatchersProvider, clock) {
    // Android requires a content:// URI to share a file with another app.
    override fun toShareableUri(path: Path): Uri = LogFileProvider.getUri(applicationContext, path)
}
