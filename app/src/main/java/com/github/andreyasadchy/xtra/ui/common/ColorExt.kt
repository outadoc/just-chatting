package com.github.andreyasadchy.xtra.ui.common

import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import kotlin.math.max
import kotlin.math.min

fun isContrastRatioAccessible(@ColorInt c1: Int, @ColorInt c2: Int): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val accessibleRatio = 7.0
        getContrastRatio(c1, c2) < accessibleRatio
    } else {
        true
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getContrastRatio(@ColorInt c1: Int, @ColorInt c2: Int): Double {
    val l1 = Color.valueOf(c1).luminance()
    val l2 = Color.valueOf(c2).luminance()

    val brightest = max(l1, l2)
    val darkest = min(l1, l2)

    return (brightest + 0.05) / (darkest + 0.05)
}
