package fr.outadoc.justchatting.feature.home.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleForDay
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.domain.model.StreamCategory
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal class ChannelScheduleDataSource(
    private val channelId: String,
    private val twitchClient: TwitchClient,
    private val start: Instant,
    private val timeZone: TimeZone,
    private val end: Instant,
) : PagingSource<ChannelScheduleDataSource.Pagination, ChannelScheduleForDay>() {

    internal sealed class Pagination {
        data class Next(
            val cursor: String,
            val lastTimeSlotOfPreviousPage: Instant? = null,
        ) : Pagination()
    }

    override fun getRefreshKey(state: PagingState<Pagination, ChannelScheduleForDay>): Pagination? =
        null

    override suspend fun load(params: LoadParams<Pagination>): LoadResult<Pagination, ChannelScheduleForDay> {
        val pagination = params.key as? Pagination.Next

        val startDate = start.toLocalDateTime(timeZone).date
        val endDate = end.toLocalDateTime(timeZone).date

        val lastTimeSlotOfPreviousPage: LocalDate =
            pagination?.lastTimeSlotOfPreviousPage?.toLocalDateTime(timeZone)?.date
                ?: startDate

        return twitchClient
            .getChannelSchedule(
                channelId = channelId,
                start = start,
                limit = params.loadSize,
                after = pagination?.cursor,
            )
            .fold(
                onSuccess = { response ->
                    val itemsAfter: Int =
                        if (response.pagination.cursor == null) {
                            0
                        } else {
                            LoadResult.Page.COUNT_UNDEFINED
                        }

                    val data: List<ChannelScheduleSegment> =
                        response.data.segments.orEmpty()
                            .map { schedule ->
                                ChannelScheduleSegment(
                                    id = schedule.id,
                                    title = schedule.title,
                                    startTime = schedule.startTime,
                                    endTime = schedule.endTime,
                                    category = schedule.category?.let { category ->
                                        StreamCategory(
                                            id = category.id,
                                            name = category.name,
                                        )
                                    },
                                    isRecurring = schedule.isRecurring,
                                )
                            }

                    val groupedData: Map<LocalDate, List<ChannelScheduleSegment>> = data
                        .groupBy { it.startTime.toLocalDateTime(timeZone).date }

                    // Either this will be the start date for the last item in the page,
                    // or if there is no next page, this will be the end date.
                    val lastTimeSlotOfPage: LocalDate =
                        groupedData.maxOfOrNull { it.key }
                            ?.takeIf { pagination?.cursor != null }
                            ?: endDate

                    // We need to return an entry for each day in the range,
                    // even if there is no data for that day.
                    val expectedDaysInPage: List<LocalDate> =
                        IntRange(
                            start = lastTimeSlotOfPreviousPage.toEpochDays(),
                            endInclusive = lastTimeSlotOfPage.toEpochDays(),
                        ).map { epochDays ->
                            LocalDate.fromEpochDays(epochDays)
                        }

                    LoadResult.Page(
                        data = expectedDaysInPage.map { date ->
                            ChannelScheduleForDay(
                                date = date,
                                segments = groupedData[date].orEmpty(),
                            )
                        },
                        prevKey = null,
                        nextKey = response.pagination.cursor?.let { cursor ->
                            Pagination.Next(
                                cursor = cursor,
                                lastTimeSlotOfPreviousPage = data.lastOrNull()?.endTime,
                            )
                        },
                        itemsAfter = itemsAfter,
                    )
                },
                onFailure = { exception ->
                    logError<ChannelScheduleDataSource>(exception) { "Error while fetching channel EPG" }

                    val emptyTimeline: List<LocalDate> =
                        IntRange(
                            start = startDate.toEpochDays(),
                            endInclusive = endDate.toEpochDays(),
                        ).map { epochDays ->
                            LocalDate.fromEpochDays(epochDays)
                        }

                    LoadResult.Page(
                        data = emptyTimeline.map { date ->
                            ChannelScheduleForDay(
                                date = date,
                                segments = emptyList(),
                            )
                        },
                        prevKey = null,
                        nextKey = null,
                        itemsAfter = 0,
                    )
                },
            )
    }
}
