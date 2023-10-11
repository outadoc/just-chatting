package fr.outadoc.justchatting.utils.core

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.fluidsonic.currency.Currency

expect fun Float.formatPercent(): String

expect fun Int.formatNumber(): String

expect fun BigDecimal.formatCurrency(currency: Currency): String
