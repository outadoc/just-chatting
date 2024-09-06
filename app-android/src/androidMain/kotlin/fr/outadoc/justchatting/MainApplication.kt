package fr.outadoc.justchatting

import android.app.Application
import android.os.Build
import coil3.ImageLoader
import coil3.ImageLoaderFactory
import coil3.decode.GifDecoder
import coil3.decode.ImageDecoderDecoder
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.transition.Transition
import coil3.util.DebugLogger
import com.google.android.material.color.DynamicColors
import fr.outadoc.justchatting.utils.logging.AndroidLogStrategy
import fr.outadoc.justchatting.utils.logging.Logger

class MainApplication :
    Application(),
    ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.ENABLE_LOGGING) {
            Logger.logStrategy = AndroidLogStrategy
        }

        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(applicationContext)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(applicationContext.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .transitionFactory(Transition.Factory.NONE)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory(enforceMinimumFrameDelay = true))
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
