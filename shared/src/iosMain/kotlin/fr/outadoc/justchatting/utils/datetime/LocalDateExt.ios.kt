package fr.outadoc.justchatting.utils.datetime

import kotlinx.datetime.number
import platform.Foundation.NSDateComponents

public fun JCLocalDate.toNSDateComponents(): NSDateComponents {
    val components = NSDateComponents()
    components.year = localDate.year.toLong()
    components.month = localDate.month.number.toLong()
    components.day = localDate.day.toLong()
    return components
}
