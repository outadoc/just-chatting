package fr.outadoc.justchatting.utils.presentation

import kotlinx.datetime.LocalDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents

public fun LocalDate.toNSDateComponents(): NSDateComponents {
    val components = NSDateComponents()
    components.year = year.toLong()
    components.month = monthNumber.toLong()
    components.day = dayOfMonth.toLong()
    return components
}
