package com.github.andreyasadchy.xtra.ui.common

import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils

fun isContrastRatioAccessible(@ColorInt background: Int, @ColorInt foreground: Int): Boolean {
    val accessibleRatio = 4.5
    val ratio = ColorUtils.calculateContrast(background, foreground)
    return ratio > accessibleRatio
}
