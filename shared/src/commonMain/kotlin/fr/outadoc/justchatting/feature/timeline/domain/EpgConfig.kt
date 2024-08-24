package fr.outadoc.justchatting.feature.timeline.domain

import kotlinx.datetime.DatePeriod

internal object EpgConfig {
    val MaxDaysAhead = DatePeriod(months = 1)
}
