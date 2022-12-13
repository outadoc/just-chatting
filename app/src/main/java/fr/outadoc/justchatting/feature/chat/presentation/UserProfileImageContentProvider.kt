package fr.outadoc.justchatting.feature.chat.presentation

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import fr.outadoc.justchatting.BuildConfig
import fr.outadoc.justchatting.component.twitch.domain.api.TwitchRepository
import fr.outadoc.justchatting.utils.logging.logDebug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import org.koin.android.ext.android.inject
import java.io.FileNotFoundException
import java.io.FileOutputStream

class UserProfileImageContentProvider : ContentProvider() {

    companion object {
        private const val authority: String =
            "${BuildConfig.APPLICATION_ID}.user-image-provider"

        private const val PATH_LOGIN = "login"

        fun createForUser(userLogin: String): Uri {
            return Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(authority)
                .appendPath(PATH_LOGIN)
                .appendPath(userLogin)
                .build()
        }
    }

    private val apiRepository by inject<TwitchRepository>()
    private val okHttpClient by inject<OkHttpClient>()

    override fun onCreate(): Boolean {
        return true
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        logDebug<UserProfileImageContentProvider> { "Called openFile($uri, $mode)" }

        if (mode != "r") {
            throw FileNotFoundException("Wrong file mode, only read-only ('r') is supported, got $mode.")
        }

        val segments = uri.pathSegments.toList()
        return when (segments.getOrNull(0)) {
            PATH_LOGIN -> {
                val userLogin: String = segments.getOrNull(1)
                    ?: throw FileNotFoundException("User login was null.")

                runBlocking(Dispatchers.IO) {
                    val profileImageUrl: String =
                        apiRepository.loadUsersByLogin(listOf(userLogin))
                            ?.firstOrNull()
                            ?.profileImageUrl
                            ?: throw FileNotFoundException("Could not retrieve user info from Twitch API.")

                    val request = okHttpClient.newCall(
                        Request(url = profileImageUrl.toHttpUrl())
                    )

                    val (inSocket, outSocket) = ParcelFileDescriptor.createSocketPair()

                    request.execute().body.source().use { source ->
                        FileOutputStream(outSocket.fileDescriptor)
                            .sink()
                            .buffer()
                            .use { sink ->
                                sink.writeAll(source)
                            }
                    }

                    ParcelFileDescriptor.fromFd(inSocket.fd)
                }
            }

            else -> {
                throw FileNotFoundException("Unsupported URI: $uri")
            }
        }
    }

    override fun getType(uri: Uri): String = "image/png"

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor =
        error("Not implemented")

    override fun insert(uri: Uri, values: ContentValues?): Uri =
        error("Not implemented")

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int =
        error("Not implemented")

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = error("Not implemented")
}
