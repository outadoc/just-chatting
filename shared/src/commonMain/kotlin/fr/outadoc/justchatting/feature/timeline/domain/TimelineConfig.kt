package fr.outadoc.justchatting.feature.timeline.domain

import kotlinx.datetime.DatePeriod

internal object TimelineConfig {
    val MaxDaysAhead = DatePeriod(days = 15)
}
