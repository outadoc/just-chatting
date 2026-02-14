package fr.outadoc.justchatting.utils.coil

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

internal actual object ImageLoaderFactory : SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader
            .Builder(context)
            .crossfade(true)
            .memoryCache {
                MemoryCache
                    .Builder()
                    .maxSizePercent(context, percent = 0.25)
                    .build()
            }.diskCache {
                DiskCache
                    .Builder()
                    .directory(getCachesDirectory().resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }.components {
                add(AnimatedSkiaImageDecoder.Factory(prerenderFrames = true))
            }.logger(CoilCustomLogger())
            .build()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun getCachesDirectory(): Path {
        return NSFileManager.defaultManager
            .URLForDirectory(
                directory = NSCachesDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )?.path
            ?.toPath()
            ?: error("Could not get caches directory")
    }
}
