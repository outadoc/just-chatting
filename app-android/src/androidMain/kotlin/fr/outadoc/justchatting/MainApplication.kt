package fr.outadoc.justchatting

import android.app.Application
import android.os.Build
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.request.crossfade
import coil3.request.transitionFactory
import coil3.transition.Transition
import coil3.util.DebugLogger
import com.google.android.material.color.DynamicColors
import fr.outadoc.justchatting.utils.logging.AndroidLogStrategy
import fr.outadoc.justchatting.utils.logging.Logger
import okio.Path.Companion.toOkioPath

class MainApplication :
    Application(),
    SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.ENABLE_LOGGING) {
            Logger.logStrategy = AndroidLogStrategy
        }

        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(applicationContext, percent = 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(applicationContext.cacheDir.toOkioPath().resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .transitionFactory(Transition.Factory.NONE)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory(enforceMinimumFrameDelay = true))
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .apply {
                if (BuildConfig.ENABLE_LOGGING) {
                    logger(DebugLogger())
                }
            }
            .build()
}
