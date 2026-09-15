package fr.outadoc.justchatting.feature.chat.presentation

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import coil3.BitmapImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.ImageResult
import coil3.request.transformations
import coil3.transform.CircleCropTransformation
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

/**
 * Downloads user profile pictures and keeps them on disk, ready to be served by
 * [UserProfileImageContentProvider].
 *
 * Those pictures are handed to other processes as content URIs, and those processes read them
 * synchronously: the launcher decodes widget images while applying our `RemoteViews`, and SystemUI
 * does the same while inflating a bubble. Fetching an image from inside the provider would
 * therefore block the caller for as long as the lookup and the download take.
 *
 * Callers instead warm the cache from their own coroutines, and only hand out the URI once
 * [isCached] reports the image is there. The provider never does more than open a file.
 */
public class ProfileImageCache internal constructor(
    private val context: Context,
    private val twitchRepository: TwitchRepository,
    dispatchersProvider: DispatchersProvider,
) {
    private companion object {
        const val USER_LOOKUP_TIMEOUT_MS = 10_000L
        const val IMAGE_SIZE_PX = 128
        const val COMPRESSION_QUALITY = 70

        /**
         * Widgets ask for every one of their items at once - Glance materializes a whole lazy list
         * into its `RemoteViews`, not just the visible part - so a freshly added widget would
         * otherwise start as many downloads as it has channels.
         */
        const val MAX_CONCURRENT_DOWNLOADS = 4
    }

    // Nothing here is worth crashing over: a missing profile picture degrades to the app icon or a
    // placeholder. Without a handler an uncaught throwable would still reach the thread's default
    // handler and take the process down, as SupervisorJob only stops sibling cancellation.
    private val scope =
        CoroutineScope(
            SupervisorJob() +
                dispatchersProvider.io +
                CoroutineExceptionHandler { _, throwable ->
                    logError<ProfileImageCache>(throwable) { "Failed to cache profile picture" }
                },
        )

    private val downloadPermits = Semaphore(MAX_CONCURRENT_DOWNLOADS)

    private val pendingMutex = Mutex()
    private val pending = mutableMapOf<String, Deferred<File?>>()

    /**
     * Whether the profile picture for [userId] can be read from disk right now, without any I/O
     * beyond opening the file.
     */
    public fun isCached(userId: String): Boolean = getCachedFile(userId) != null

    /**
     * Downloads the profile picture for [userId] unless it is already cached, and returns whether
     * it is available afterwards.
     *
     * Concurrent calls for the same user share a single download, and the download outlives the
     * caller that started it: a cancelled composition does not waste the work done so far.
     */
    public suspend fun fetch(userId: String): Boolean {
        if (isCached(userId)) return true

        val download: Deferred<File?> =
            pendingMutex.withLock {
                // Completed downloads are dropped here rather than when they finish, so that this
                // map never grows past the number of users we are currently fetching.
                pending.values.removeAll { download -> download.isCompleted }
                pending.getOrPut(userId) { scope.async { downloadCatching(userId) } }
            }

        return download.await() != null
    }

    /**
     * Starts caching the profile picture for [userId] without waiting for it.
     */
    public fun prefetch(userId: String) {
        scope.launch { fetch(userId) }
    }

    internal fun getCachedFile(userId: String): File? =
        getFile(userId).takeIf { file ->
            // A previous run may have been interrupted mid-write, or the OS may have wiped the
            // cache directory; treat an empty file as missing rather than handing back something
            // that will fail to decode forever.
            file.exists() && file.length() > 0
        }

    private suspend fun downloadCatching(userId: String): File? =
        try {
            download(userId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logError<ProfileImageCache>(e) { "Failed to cache profile picture for user $userId" }
            null
        }

    private suspend fun download(userId: String): File? = downloadPermits.withPermit { fetchAndStore(userId) }

    private suspend fun fetchAndStore(userId: String): File? {
        // getUserById is backed by the local database and emits a failure while the user is still
        // being synced, so wait for the first emission that actually carries an image rather than
        // giving up on the first one.
        val profileImageUrl: String =
            withTimeoutOrNull(USER_LOOKUP_TIMEOUT_MS) {
                twitchRepository
                    .getUserById(userId)
                    .mapNotNull { result ->
                        result.getOrNull()?.profileImageUrl?.takeIf { url -> url.isNotBlank() }
                    }.first()
            } ?: run {
                logError<ProfileImageCache> { "No profile picture available for user $userId" }
                return null
            }

        val response: ImageResult =
            context.imageLoader.execute(
                ImageRequest
                    .Builder(context)
                    .data(profileImageUrl)
                    .size(IMAGE_SIZE_PX)
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
        // a truncated image behind: the provider would keep serving it for as long as the cache
        // lives. The name has to be unique, not just per-user, so that two writers can never
        // publish each other's half-written bytes.
        val destination = getFile(userId)
        val temporaryFile = File.createTempFile("user_image_", ".tmp", destination.parentFile)

        try {
            temporaryFile.outputStream().use { os ->
                check(bitmap.compress(format, COMPRESSION_QUALITY, os)) {
                    "Failed to compress profile picture for user $userId"
                }
            }

            check(temporaryFile.renameTo(destination)) {
                "Failed to move profile picture for user $userId into place"
            }
        } finally {
            temporaryFile.delete()
        }

        return destination
    }

    private fun getFile(userId: String): File {
        val directory: File =
            File(context.cacheDir, "user_images").apply {
                if (!exists()) {
                    mkdir()
                }
            }

        return directory.resolve("user_image_$userId.webp")
    }
}
