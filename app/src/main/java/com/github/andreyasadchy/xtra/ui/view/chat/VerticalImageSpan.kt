package com.github.andreyasadchy.xtra.ui.view.chat

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.style.ImageSpan
import androidx.core.graphics.withSave

/**
 * A vertically-aligned version of [ImageSpan], so that images
 * are properly aligned with the text they accompany.
 *
 * Credits: https://stackoverflow.com/a/38788432/3136280
 */
class VerticalImageSpan(drawable: Drawable) : ImageSpan(drawable) {

    override fun getSize(
        paint: Paint,
        text: CharSequence?,
        start: Int,
        end: Int,
        fontMetricsInt: Paint.FontMetricsInt?
    ): Int {
        val drawableBounds: Rect = drawable.bounds

        fontMetricsInt?.let { fm ->
            val fmPaint = paint.fontMetricsInt
            val fontHeight: Int = fmPaint.descent - fmPaint.ascent
            val drawableHeight: Int = drawableBounds.bottom - drawableBounds.top
            val centerY: Int = fmPaint.ascent + fontHeight / 2

            fm.ascent = centerY - drawableHeight / 2
            fm.top = fm.ascent
            fm.bottom = centerY + drawableHeight / 2
            fm.descent = fm.bottom
        }

        return drawableBounds.right
    }

    override fun draw(
        canvas: Canvas,
        text: CharSequence?,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        val drawable = drawable
        canvas.withSave {
            val fmPaint = paint.fontMetricsInt
            val fontHeight: Int = fmPaint.descent - fmPaint.ascent
            val centerY: Int = y + fmPaint.descent - fontHeight / 2
            val transY: Float = centerY - (drawable.bounds.bottom - drawable.bounds.top) / 2f
            translate(x, transY)
            drawable.draw(this)
        }
    }
}
