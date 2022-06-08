package com.github.andreyasadchy.xtra.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.signature.ObjectKey
import com.github.andreyasadchy.xtra.GlideApp

@SuppressLint("CheckResult")
fun ImageView.loadImage(
    context: Context,
    url: String?,
    changes: Boolean = false,
    circle: Boolean = false,
    diskCacheStrategy: DiskCacheStrategy = DiskCacheStrategy.RESOURCE
) {
    if (context.isActivityResumed) { // not enough on some devices?
        try {
            val request = GlideApp.with(context)
                .load(url)
                .diskCacheStrategy(diskCacheStrategy)
                .transition(DrawableTransitionOptions.withCrossFade())
            if (changes) {
                // update every 5 minutes
                val minutes = System.currentTimeMillis() / 60000L
                val lastMinute = minutes % 10
                val key = if (lastMinute < 5) minutes - lastMinute else minutes - (lastMinute - 5)
                request.signature(ObjectKey(key))
            }
            if (circle) {
                request.circleCrop()
            }
            request.into(this)
        } catch (e: IllegalArgumentException) {
        }
        return
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

val View.isKeyboardShown: Boolean
    get() {
        val rect = Rect()
        getWindowVisibleDisplayFrame(rect)
        val screenHeight = rootView.height

        // rect.bottom is the position above soft keypad or device button.
        // if keypad is shown, the r.bottom is smaller than that before.
        val keypadHeight = screenHeight - rect.bottom
        return keypadHeight > screenHeight * 0.15
    }

class TextWithCanvas(context: Context, attrs: AttributeSet) : AppCompatTextView(context, attrs) {
    override fun draw(canvas: Canvas?) {
        for (i in 0..6) {
            super.draw(canvas)
        }
    }
}
