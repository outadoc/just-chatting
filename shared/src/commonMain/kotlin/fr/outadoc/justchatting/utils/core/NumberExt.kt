package fr.outadoc.justchatting.utils.core

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.fluidsonic.currency.Currency

val Int.isOdd: Boolean get() = this % 2 == 1

fun Int.roundUpOddToEven(): Int = if (isOdd) this + 1 else this

expect fun Int.formatNumber(): String

expect fun BigDecimal.formatCurrency(currency: Currency): String
