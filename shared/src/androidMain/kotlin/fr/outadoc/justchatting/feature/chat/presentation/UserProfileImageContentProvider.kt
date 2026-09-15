package fr.outadoc.justchatting.feature.chat.presentation

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import fr.outadoc.justchatting.utils.logging.logDebug
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileNotFoundException

public class UserProfileImageContentProvider : ContentProvider() {
    public companion object {
        private const val PATH_ID = "id"

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

    private val profileImageCache by inject<ProfileImageCache>()

    override fun onCreate(): Boolean = true

    override fun openFile(
        uri: Uri,
        mode: String,
    ): ParcelFileDescriptor? {
        logDebug<UserProfileImageContentProvider> { "Called openFile($uri, $mode)" }

        if (mode != "r") {
            throw FileNotFoundException("Wrong file mode, only read-only ('r') is supported, got $mode.")
        }

        val segments = uri.pathSegments.toList()
        return when (segments.getOrNull(0)) {
            PATH_ID -> {
                val userId: String =
                    segments.getOrNull(1)
                        ?: throw FileNotFoundException("User id was null.")

                // This runs on a binder thread, and the caller blocks until we return: the launcher
                // decodes widget images while applying our RemoteViews, and SystemUI does the same
                // while inflating a bubble. Downloading here would freeze their UI, so only ever
                // serve what is already on disk, and let the fetch happen in the background for
                // whoever asks next.
                val file: File =
                    profileImageCache.getCachedFile(userId)
                        ?: run {
                            profileImageCache.prefetch(userId)
                            throw FileNotFoundException("Profile picture for user $userId is not cached yet.")
                        }

                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            }

            else -> {
                throw FileNotFoundException("Unsupported URI: $uri")
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
