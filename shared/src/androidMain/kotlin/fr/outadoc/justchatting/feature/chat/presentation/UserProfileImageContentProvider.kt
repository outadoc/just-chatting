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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileNotFoundException

public class UserProfileImageContentProvider : ContentProvider() {
    internal companion object {
        private const val PATH_ID = "id"

        fun createForUser(
            context: Context,
            userId: String,
        ): Uri {
            return Uri
                .Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority("${context.applicationContext.packageName}.user-image-provider")
                .appendPath(PATH_ID)
                .appendPath(userId)
                .build()
        }
    }

    private val apiRepository by inject<TwitchRepository>()

    override fun onCreate(): Boolean {
        return true
    }

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

                if (!file.exists()) {
                    runBlocking(DispatchersProvider.io) {
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
    ) {
        return withContext(DispatchersProvider.io) {
            val profileImageUrl: String =
                apiRepository
                    .getUserById(userId)
                    .firstOrNull()
                    ?.getOrNull()
                    ?.profileImageUrl
                    ?: throw FileNotFoundException("User not found: $userId")

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

            getFile(context, userId).outputStream().use { os ->
                bitmap.compress(format, 70, os)
            }
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
