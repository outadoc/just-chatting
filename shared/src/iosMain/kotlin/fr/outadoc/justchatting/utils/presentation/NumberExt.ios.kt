package fr.outadoc.justchatting.utils.presentation

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSNumberFormatterPercentStyle

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
