package fr.outadoc.justchatting.utils.presentation

import java.text.NumberFormat
import java.util.Locale

internal actual fun Float.formatPercent(): String =
    NumberFormat.getPercentInstance(Locale.getDefault()).format(this)

internal actual fun Int.formatNumber(): String = "%,d".format(this)
