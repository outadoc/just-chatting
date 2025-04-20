package fr.outadoc.justchatting.utils.coil

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader

internal actual object ImageLoaderFactory : SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        TODO("Not yet implemented")
    }
}
