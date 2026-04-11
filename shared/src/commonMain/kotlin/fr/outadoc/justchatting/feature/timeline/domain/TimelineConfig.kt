package fr.outadoc.justchatting.feature.timeline.domain

import kotlinx.datetime.DatePeriod

public object TimelineConfig {
    public val MaxDaysAhead: DatePeriod = DatePeriod(days = 15)
}
