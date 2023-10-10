package fr.outadoc.justchatting.utils.core

import java.text.NumberFormat
import java.util.Locale

val Int.isOdd: Boolean get() = this % 2 == 1

fun Int.roundUpOddToEven(): Int = if (isOdd) this + 1 else this

fun Int.formatNumber(): String = "%,d".format(this)

fun Float.formatPercent(): String =
    NumberFormat.getPercentInstance(Locale.getDefault()).format(this)
