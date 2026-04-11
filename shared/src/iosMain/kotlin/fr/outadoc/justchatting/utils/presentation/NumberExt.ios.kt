package fr.outadoc.justchatting.utils.presentation

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSNumberFormatterPercentStyle

public actual fun Float.formatPercent(): String = NSNumberFormatter.localizedStringFromNumber(
    NSNumber(this),
    NSNumberFormatterPercentStyle,
)

public actual fun Int.formatNumber(): String = NSNumberFormatter.localizedStringFromNumber(
    NSNumber(this),
    NSNumberFormatterDecimalStyle,
)
