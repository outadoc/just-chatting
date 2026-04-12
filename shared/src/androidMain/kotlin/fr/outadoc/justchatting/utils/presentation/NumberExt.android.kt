package fr.outadoc.justchatting.utils.presentation

import java.text.NumberFormat
import java.util.Locale

public actual fun Float.formatPercent(): String =
    NumberFormat.getPercentInstance(Locale.getDefault()).format(this)

public actual fun Int.formatNumber(): String = "%,d".format(this)
