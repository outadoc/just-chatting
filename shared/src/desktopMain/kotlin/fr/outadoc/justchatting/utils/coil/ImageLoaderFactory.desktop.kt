package fr.outadoc.justchatting.utils.coil

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.crossfade
import fr.outadoc.justchatting.AppInfo
import net.harawata.appdirs.AppDirsFactory
import okio.Path.Companion.toPath

internal actual object ImageLoaderFactory : SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, percent = 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(
                        AppDirsFactory.getInstance()
                            .getUserCacheDir(
                                AppInfo.APP_ID,
                                AppInfo.APP_VERSION,
                                AppInfo.APP_AUTHOR,
                            )
                            .toPath()
                            .resolve("image_cache"),
                    )
                    .maxSizePercent(0.02)
                    .build()
            }
            .components {
                // TODO move AnimatedSkiaImageDecoder
                // add(AnimatedSkiaImageDecoder.Factory(prerenderFrames = true))
            }
            .logger(CoilCustomLogger())
            .build()
    }
}
