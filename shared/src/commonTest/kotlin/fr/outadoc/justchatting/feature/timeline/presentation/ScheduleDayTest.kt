package fr.outadoc.justchatting.feature.timeline.presentation

import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.DaySchedule
import fr.outadoc.justchatting.utils.datetime.JCLocalDate
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

internal class ScheduleDayTest {
    private val now = Instant.parse("2022-01-01T18:00:00Z")
    private val today = LocalDate(2022, 1, 1)

    private val user =
        User(
            id = "1",
            login = "maghla",
            displayName = "Maghla",
            description = "",
            profileImageUrl = "",
            createdAt = Instant.DISTANT_PAST,
            usedAt = Instant.DISTANT_PAST,
        )

    private fun segment(
        id: String,
        start: String,
        end: String?,
    ) = ChannelScheduleSegment(
        id = id,
        user = user,
        startTime = Instant.parse(start),
        endTime = end?.let(Instant::parse),
        title = "",
    )

    private fun day(
        date: LocalDate,
        vararg segments: ChannelScheduleSegment,
    ) = DaySchedule(
        date = JCLocalDate(date),
        schedule = segments.toList(),
    )

    private fun List<DaySchedule>.split() = toScheduleDays(now = now, timeZone = TimeZone.UTC)

    @Test
    fun `today is split into ongoing and upcoming segments, without the ones that ended`() {
        val ended = segment("ended", start = "2022-01-01T10:00:00Z", end = "2022-01-01T12:00:00Z")
        val ongoing = segment("ongoing", start = "2022-01-01T17:00:00Z", end = "2022-01-01T19:00:00Z")
        val upcoming = segment("upcoming", start = "2022-01-01T20:00:00Z", end = "2022-01-01T22:00:00Z")

        val result = listOf(day(today, ended, ongoing, upcoming)).split().single()

        assertTrue(result.isToday)
        assertEquals(listOf(ongoing), result.ongoing.map { it.segment })
        assertEquals(listOf(upcoming), result.upcoming)
    }

    @Test
    fun `progress is how far along the segment is`() {
        val segment = segment("1", start = "2022-01-01T17:00:00Z", end = "2022-01-01T21:00:00Z")

        val result = listOf(day(today, segment)).split().single()

        assertEquals(0.25f, result.ongoing.single().progress)
    }

    @Test
    fun `a segment is ongoing from the moment it starts`() {
        val segment = segment("1", start = "2022-01-01T18:00:00Z", end = "2022-01-01T20:00:00Z")

        val result = listOf(day(today, segment)).split().single()

        assertEquals(0f, result.ongoing.single().progress)
        assertEquals(emptyList(), result.upcoming)
    }

    @Test
    fun `a segment is no longer ongoing once it ends`() {
        val segment = segment("1", start = "2022-01-01T16:00:00Z", end = "2022-01-01T18:00:00Z")

        val result = listOf(day(today, segment)).split().single()

        assertEquals(emptyList(), result.ongoing)
        assertEquals(emptyList(), result.upcoming)
    }

    @Test
    fun `a started segment without an end time is ongoing, with unknown progress`() {
        val segment = segment("1", start = "2022-01-01T12:00:00Z", end = null)

        val result = listOf(day(today, segment)).split().single()

        assertEquals(listOf(OngoingScheduleSegment(segment, progress = null)), result.ongoing)
    }

    @Test
    fun `later days only have upcoming segments`() {
        val tomorrow = LocalDate(2022, 1, 2)
        val segment = segment("1", start = "2022-01-02T10:00:00Z", end = "2022-01-02T12:00:00Z")

        val result = listOf(day(tomorrow, segment)).split().single()

        assertEquals(1, result.daysFromToday)
        assertEquals(emptyList(), result.ongoing)
        assertEquals(listOf(segment), result.upcoming)
    }

    @Test
    fun `days before today are dropped`() {
        val yesterday = LocalDate(2021, 12, 31)

        val result = listOf(day(yesterday), day(today)).split()

        assertEquals(listOf(0), result.map { it.daysFromToday })
    }

    @Test
    fun `today is computed in the given time zone`() {
        // 18:00 UTC is already the next day in Tokyo.
        val result =
            listOf(day(today), day(LocalDate(2022, 1, 2)))
                .toScheduleDays(now = now, timeZone = TimeZone.of("Asia/Tokyo"))

        assertEquals(listOf(0), result.map { it.daysFromToday })
    }
}
