package fr.outadoc.justchatting.utils.core

internal val Int.isOdd: Boolean get() = this % 2 == 1

internal val Int.isEven: Boolean get() = this % 2 == 0

internal fun Int.roundUpOddToEven(): Int = if (isOdd) this + 1 else this
