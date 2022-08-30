package fr.outadoc.justchatting.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation

@SuppressLint("CheckResult")
fun ImageView.loadImage(
    context: Context,
    url: String?,
    circle: Boolean = false
) {
    // not enough on some devices?
    if (!context.isActivityResumed) return

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

@SuppressLint("CheckResult")
fun loadImage(
    context: Context,
    url: String?,
    circle: Boolean = false,
    width: Int,
    height: Int,
    listener: (Drawable) -> Unit
) {
    // not enough on some devices?
    if (!context.isActivityResumed) return

    try {
        val request = ImageRequest.Builder(context)
            .data(url)
            .crossfade(true)
            .size(width, height)
            .apply {
                if (circle) {
                    transformations(CircleCropTransformation())
                }
            }
            .target(onSuccess = listener)
            .build()

        context.imageLoader.enqueue(request)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
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
