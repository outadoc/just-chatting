package fr.outadoc.justchatting.feature.preferences.presentation

import android.content.Context
import androidx.core.content.FileProvider
import fr.outadoc.justchatting.shared.R
import okio.Path

class LogFileProvider : FileProvider(R.xml.log_share) {

    companion object {

        fun getUri(context: Context, path: Path): String =
            getUriForFile(
                context,
                "${context.packageName}.logfileprovider",
                path.toFile(),
            ).toString()
    }
}
