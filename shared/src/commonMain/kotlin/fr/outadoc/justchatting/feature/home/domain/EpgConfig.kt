package fr.outadoc.justchatting.feature.home.domain

import kotlinx.datetime.DatePeriod

internal object EpgConfig {
    val MaxDaysAhead = DatePeriod(months = 1)
}
