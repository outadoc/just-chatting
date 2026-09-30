package fr.outadoc.justchatting.utils.core

import kotlinx.datetime.TimeZone

interface TimeZoneProvider {
    /**
     * The user's current time zone. It can change while the app is running, so it should be
     * read every time it's needed rather than stored.
     */
    val currentTimeZone: TimeZone
}
