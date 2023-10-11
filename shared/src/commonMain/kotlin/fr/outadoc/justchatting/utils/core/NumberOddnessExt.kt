package fr.outadoc.justchatting.utils.core

val Int.isOdd: Boolean get() = this % 2 == 1

fun Int.roundUpOddToEven(): Int = if (isOdd) this + 1 else this
