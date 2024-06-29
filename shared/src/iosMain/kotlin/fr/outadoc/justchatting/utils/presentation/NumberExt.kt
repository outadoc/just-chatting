package fr.outadoc.justchatting.utils.presentation

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.fluidsonic.currency.Currency
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSNumberFormatterPercentStyle
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

internal actual fun Float.formatPercent(): String =
    NSNumberFormatter.localizedStringFromNumber(
        NSNumber(this),
        NSNumberFormatterPercentStyle,
    )

internal actual fun Int.formatNumber(): String =
    NSNumberFormatter.localizedStringFromNumber(
        NSNumber(this),
        NSNumberFormatterDecimalStyle,
    )

internal actual fun BigDecimal.formatCurrency(currency: Currency): String {
    val amount = NSNumber(this.floatValue(exactRequired = false))
    val currentLocale = NSLocale.currentLocale().localeIdentifier()
    val formatter = NSNumberFormatter().apply {
        numberStyle = NSNumberFormatterCurrencyStyle
        locale = NSLocale(
            localeIdentifier = "$currentLocale@currency=${currency.code}",
        )
    }
    return formatter.stringFromNumber(amount)
        ?: "${this.toPlainString()} ${currency.code}"
}
