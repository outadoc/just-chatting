package fr.outadoc.justchatting.utils.core

import kotlinx.datetime.TimeZone

internal class DefaultTimeZoneProvider : TimeZoneProvider {
    override val currentTimeZone: TimeZone
        get() = TimeZone.currentSystemDefault()
}
