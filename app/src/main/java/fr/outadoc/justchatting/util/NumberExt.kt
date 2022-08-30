package fr.outadoc.justchatting.util

val Int.isOdd: Boolean get() = this % 2 == 1

fun Int.formatNumber(): String = "%,d".format(this)
