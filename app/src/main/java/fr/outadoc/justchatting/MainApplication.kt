package fr.outadoc.justchatting

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.transition.Transition
import coil.util.DebugLogger
import com.google.android.material.color.DynamicColors
import fr.outadoc.justchatting.utils.logging.AndroidLogStrategy
import fr.outadoc.justchatting.utils.logging.Logger

class MainApplication : Application(), ImageLoaderFactory {

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
