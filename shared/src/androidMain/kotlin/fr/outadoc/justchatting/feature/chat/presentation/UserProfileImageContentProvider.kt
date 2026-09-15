package fr.outadoc.justchatting.feature.chat.presentation

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import coil3.BitmapImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logDebug
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileNotFoundException

public class UserProfileImageContentProvider : ContentProvider() {
    public companion object {
        private const val PATH_ID = "id"

        private const val USER_LOOKUP_TIMEOUT_MS = 10_000L

        public fun createForUser(
            context: Context,
            userId: String,
        ): Uri =
            Uri
                .Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority("${context.applicationContext.packageName}.user-image-provider")
                .appendPath(PATH_ID)
                .appendPath(userId)
                .build()
    }

    private val apiRepository by inject<TwitchRepository>()
    private val dispatchersProvider by inject<DispatchersProvider>()

    override fun onCreate(): Boolean = true

    override fun openFile(
        uri: Uri,
        mode: String,
    ): ParcelFileDescriptor? {
        logDebug<UserProfileImageContentProvider> { "Called openFile($uri, $mode)" }

        if (mode != "r") {
            throw FileNotFoundException("Wrong file mode, only read-only ('r') is supported, got $mode.")
        }

        val context: Context =
            context ?: error("Context was null when in openFile")

        val segments = uri.pathSegments.toList()
        return when (segments.getOrNull(0)) {
            PATH_ID -> {
                val userId: String =
                    segments.getOrNull(1)
                        ?: throw FileNotFoundException("User id was null.")

                val file = getFile(context, userId)

                // A previous run may have been interrupted mid-write, or the OS may have wiped the
                // cache directory; treat an empty file as missing rather than handing back something
                // that will fail to decode forever.
                if (!file.exists() || file.length() == 0L) {
                    runBlocking(dispatchersProvider.io) {
                        downloadImage(context, userId)
                    }
                }

                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            }

            else -> {
                throw FileNotFoundException("Unsupported URI: $uri")
            }
        }
    }

    private fun getFile(
        context: Context,
        userId: String,
    ): File {
        val directory: File =
            File(context.cacheDir, "user_images").apply {
                if (!exists()) {
                    mkdir()
                }
            }

        return directory.resolve("user_image_$userId.webp")
    }

    private suspend fun downloadImage(
        context: Context,
        userId: String,
    ) = withContext(dispatchersProvider.io) {
        // getUserById is backed by the local database and emits a failure while the user is still
        // being synced, so wait for the first emission that actually carries an image rather than
        // giving up on the first one. This runs on a binder thread, hence the bound.
        val profileImageUrl: String =
            try {
                withTimeoutOrNull(USER_LOOKUP_TIMEOUT_MS) {
                    apiRepository
                        .getUserById(userId)
                        .mapNotNull { result ->
                            result.getOrNull()?.profileImageUrl?.takeIf { url -> url.isNotBlank() }
                        }.first()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logError<UserProfileImageContentProvider>(e) { "Failed to look up profile image for $userId" }
                null
            } ?: throw FileNotFoundException("No profile image available for user $userId")

        val response: ImageResult =
            context.imageLoader.execute(
                ImageRequest
                    .Builder(context)
                    .data(profileImageUrl)
                    .size(128)
                    .transformations(CircleCropTransformation())
                    .build(),
            )

        val bitmap: Bitmap =
            (response.image as? BitmapImage)?.bitmap
                ?: error("Empty bitmap received from Coil")

        val format =
            if (Build.VERSION.SDK_INT >= 30) {
                Bitmap.CompressFormat.WEBP_LOSSLESS
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }

        // Write to a temporary file and move it into place, so an interrupted write can never leave
        // a truncated image behind: openFile would keep serving it for as long as the cache lives.
        // The name has to be unique, not just per-user: openFile runs on binder threads and the
        // same image is regularly requested concurrently, and two writers sharing one temporary
        // file would publish each other's half-written bytes.
        val destination = getFile(context, userId)
        val temporaryFile = File.createTempFile("user_image_", ".tmp", destination.parentFile)

        try {
            temporaryFile.outputStream().use { os ->
                check(bitmap.compress(format, 70, os)) { "Failed to compress profile image for $userId" }
            }

            check(temporaryFile.renameTo(destination)) {
                "Failed to move profile image for $userId into place"
            }
        } finally {
            temporaryFile.delete()
        }
    }

    override fun getType(uri: Uri): String = "image/webp"

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor = error("Not implemented")

    override fun insert(
        uri: Uri,
        values: ContentValues?,
    ): Uri = error("Not implemented")

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = error("Not implemented")

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = error("Not implemented")
}
