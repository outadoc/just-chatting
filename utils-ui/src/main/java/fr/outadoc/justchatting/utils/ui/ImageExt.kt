package fr.outadoc.justchatting.utils.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.suspendCancellableCoroutine

@SuppressLint("CheckResult")
fun ImageView.loadImage(
    url: String?,
    circle: Boolean = false
) {
    try {
        val request = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .apply {
                if (circle) {
                    transformations(CircleCropTransformation())
                }
            }
            .target(this)
            .build()

        context.imageLoader.enqueue(request)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
    }
}

suspend fun loadImageToBitmap(
    context: Context,
    imageUrl: String,
    circle: Boolean = false,
    width: Int,
    height: Int
): Bitmap? {
    return suspendCancellableCoroutine { cont ->
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .size(width, height)
            .apply {
                if (circle) transformations(CircleCropTransformation())
            }
            .target(
                onSuccess = { drawable ->
                    cont.resumeWith(
                        Result.success((drawable as? BitmapDrawable)?.bitmap)
                    )
                },
                onError = { drawable ->
                    cont.resumeWith(
                        Result.success((drawable as? BitmapDrawable)?.bitmap)
                    )
                }
            )
            .build()

        if (!cont.isCancelled) {
            val disposable = context.imageLoader.enqueue(request)
            cont.invokeOnCancellation { disposable.dispose() }
        }
    }
}
