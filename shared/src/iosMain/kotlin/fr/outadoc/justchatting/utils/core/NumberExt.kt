package fr.outadoc.justchatting.utils.core

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.fluidsonic.currency.Currency
import platform.Foundation.NSLocale
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.currentLocale
import platform.Foundation.localeIdentifier

actual fun Int.formatNumber(): String =
    NSNumberFormatter.localizedStringFromNumber(
        NSNumber(this),
        NSNumberFormatterDecimalStyle,
    )

actual fun BigDecimal.formatCurrency(currency: Currency): String {
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
