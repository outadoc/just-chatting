package fr.outadoc.justchatting.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
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
): BitmapDrawable? {
    return suspendCancellableCoroutine { cont ->
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .size(width, height)
            .apply {
                if (circle) transformations(CircleCropTransformation())
            }
            .target(
                onSuccess = { drawable ->
                    cont.resumeWith(
                        Result.success(drawable as? BitmapDrawable)
                    )
                },
                onError = { drawable ->
                    cont.resumeWith(
                        Result.success(drawable as? BitmapDrawable)
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

fun EditText.showKeyboard() {
    requestFocus()
    val imm: InputMethodManager? =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    this.postDelayed({
        imm?.showSoftInput(this, 0)
    }, 100)
}

fun SearchView.showKeyboard() {
    val imm: InputMethodManager? =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    this.postDelayed({
        this.isIconified = false
        imm?.showSoftInput(this, 0)
    }, 100)
}

fun View.hideKeyboard() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
        windowToken,
        0
    )
}
