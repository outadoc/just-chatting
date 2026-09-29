package fr.outadoc.justchatting.feature.timeline.presentation

import androidx.compose.runtime.Immutable
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.DaySchedule
import fr.outadoc.justchatting.utils.datetime.JCLocalDate
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * The schedule of a single day, split the way it is displayed.
 */
@Immutable
public data class ScheduleDay(
    val date: JCLocalDate,
    /** 0 for today, 1 for tomorrow, and so on. */
    val daysFromToday: Int,
    /** Segments happening right now. Only today can have any. */
    val ongoing: ImmutableList<OngoingScheduleSegment>,
    /** Segments that haven't started yet. Today's segments that already ended are left out. */
    val upcoming: ImmutableList<ChannelScheduleSegment>,
) {
    val isToday: Boolean
        get() = daysFromToday == 0
}

@Immutable
public data class OngoingScheduleSegment(
    val segment: ChannelScheduleSegment,
    /** How far along the segment is, from 0 to 1, or null if its end time is unknown. */
    val progress: Float?,
)

/**
 * Splits each day's schedule into what's happening at [now] and what's still to come, dropping
 * days before today.
 *
 * A segment without an end time is considered ongoing as soon as it has started.
 */
internal fun List<DaySchedule>.toScheduleDays(
    now: Instant,
    timeZone: TimeZone,
): ImmutableList<ScheduleDay> {
    val today = now.toLocalDateTime(timeZone).date

    return mapNotNull { daySchedule ->
        val daysFromToday = today.daysUntil(daySchedule.date.localDate)
        when {
            daysFromToday < 0 -> {
                null
            }

            daysFromToday == 0 -> {
                ScheduleDay(
                    date = daySchedule.date,
                    daysFromToday = 0,
                    ongoing =
                        daySchedule.schedule
                            .filter { segment -> segment.isOngoingAt(now) }
                            .map { segment ->
                                OngoingScheduleSegment(
                                    segment = segment,
                                    progress = segment.progressAt(now),
                                )
                            }.toImmutableList(),
                    upcoming =
                        daySchedule.schedule
                            .filter { segment -> segment.startTime > now }
                            .toImmutableList(),
                )
            }

            else -> {
                ScheduleDay(
                    date = daySchedule.date,
                    daysFromToday = daysFromToday,
                    ongoing = persistentListOf(),
                    upcoming = daySchedule.schedule.toImmutableList(),
                )
            }
        }
    }.toImmutableList()
}

private fun ChannelScheduleSegment.isOngoingAt(now: Instant): Boolean {
    val endTime = endTime
    return startTime <= now && (endTime == null || now < endTime)
}

private fun ChannelScheduleSegment.progressAt(now: Instant): Float? {
    val endTime = endTime ?: return null
    val total = endTime - startTime
    if (!total.isPositive()) return null
    return ((now - startTime) / total).toFloat().coerceIn(0f, 1f)
}
