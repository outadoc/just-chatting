package fr.outadoc.justchatting.utils.core

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.fluidsonic.currency.Currency
import io.fluidsonic.currency.toPlatform
import java.text.NumberFormat
import java.util.Locale

actual fun Float.formatPercent(): String =
    NumberFormat.getPercentInstance(Locale.getDefault()).format(this)

actual fun Int.formatNumber(): String = "%,d".format(this)

actual fun BigDecimal.formatCurrency(currency: Currency): String =
    NumberFormat.getCurrencyInstance()
        .apply { this.currency = currency.toPlatform() }
        .format(this)
