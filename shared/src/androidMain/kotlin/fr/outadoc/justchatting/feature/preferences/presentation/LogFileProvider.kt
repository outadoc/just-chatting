package fr.outadoc.justchatting.feature.preferences.presentation

import android.content.Context
import androidx.core.content.FileProvider
import com.eygraber.uri.Uri
import com.eygraber.uri.toKmpUri
import fr.outadoc.justchatting.shared.R
import okio.Path

internal class LogFileProvider : FileProvider(R.xml.log_share) {
    companion object {
        fun getUri(
            context: Context,
            path: Path,
        ): Uri =
            getUriForFile(
                context,
                "${context.packageName}.logfileprovider",
                path.toFile(),
            ).toKmpUri()
    }
}
