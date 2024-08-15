package fr.outadoc.justchatting.feature.chat.presentation

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.ParcelFileDescriptor
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.transform.CircleCropTransformation
import fr.outadoc.justchatting.feature.home.domain.TwitchRepository
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logDebug
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import java.io.FileNotFoundException
import java.io.FileOutputStream

public class UserProfileImageContentProvider : ContentProvider() {

    internal companion object {

        private const val PATH_ID = "id"

        fun createForUser(context: Context, userId: String): Uri {
            return Uri.Builder()
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

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        logDebug<UserProfileImageContentProvider> { "Called openFile($uri, $mode)" }

        if (mode != "r") {
            throw FileNotFoundException("Wrong file mode, only read-only ('r') is supported, got $mode.")
        }

        val context: Context =
            context ?: error("Context was null when in openFile")

        val segments = uri.pathSegments.toList()
        return when (segments.getOrNull(0)) {
            PATH_ID -> {
                val userId: String = segments.getOrNull(1)
                    ?: throw FileNotFoundException("User id was null.")

                runBlocking(DispatchersProvider.io) {
                    val profileImageUrl: String =
                        apiRepository.getUserById(userId)
                            .firstOrNull()
                            ?.getOrNull()
                            ?.profileImageUrl
                            ?: throw FileNotFoundException("User not found: $userId")

                    val response: ImageResult =
                        context.imageLoader.execute(
                            ImageRequest.Builder(context)
                                .data(profileImageUrl)
                                .size(128)
                                .transformations(CircleCropTransformation())
                                .build(),
                        )

                    val bitmap: Bitmap = (response.drawable as? BitmapDrawable)?.bitmap
                        ?: error("Empty bitmap received from Coil")

                    // Create a socket pair, one for writing the bitmap and the other for reading it back
                    val (inSocket, outSocket) = ParcelFileDescriptor.createSocketPair()

                    // Write resulting bitmap to output stream
                    val os = FileOutputStream(outSocket.fileDescriptor)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, os)

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
        sortOrder: String?,
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
        selectionArgs: Array<out String>?,
    ): Int = error("Not implemented")
}
