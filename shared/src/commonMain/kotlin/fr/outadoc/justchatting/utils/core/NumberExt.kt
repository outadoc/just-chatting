package fr.outadoc.justchatting.utils.core

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.fluidsonic.currency.Currency

internal expect fun Float.formatPercent(): String

internal expect fun Int.formatNumber(): String

internal expect fun BigDecimal.formatCurrency(currency: Currency): String
