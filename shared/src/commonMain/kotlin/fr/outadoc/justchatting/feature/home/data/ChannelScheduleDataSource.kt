package fr.outadoc.justchatting.feature.home.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleForDay
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.domain.model.StreamCategory
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

internal class ChannelScheduleDataSource(
    private val channelId: String,
    private val twitchClient: TwitchClient,
    private val start: Instant,
    private val timeZone: TimeZone,
    private val pastRange: DatePeriod,
    private val futureRange: DatePeriod,
) : PagingSource<ChannelScheduleDataSource.Pagination, ChannelScheduleForDay>() {

    internal sealed class Pagination {

        data class Past(
            val cursor: String? = null,
        ) : Pagination()

        data class Future(
            val cursor: String? = null,
            val lastTimeSlotOfPreviousPage: Instant? = null,
        ) : Pagination()
    }

    override fun getRefreshKey(state: PagingState<Pagination, ChannelScheduleForDay>): Pagination? =
        null

    override suspend fun load(params: LoadParams<Pagination>): LoadResult<Pagination, ChannelScheduleForDay> {
        return when (val pagination = params.key) {
            is Pagination.Past -> {
                // Load previous videos
                loadPast(
                    loadSize = params.loadSize,
                    pagination = params.key as Pagination.Past,
                )
            }

            is Pagination.Future -> {
                // Load EPG data
                loadFuture(
                    loadSize = params.loadSize,
                    pagination = pagination,
                )
            }

            null -> {
                // Initial load
                loadFuture(
                    loadSize = params.loadSize,
                    pagination = Pagination.Future(),
                )
            }
        }
    }

    private fun loadPast(
        loadSize: Int,
        pagination: Pagination.Past,
    ): LoadResult<Pagination, ChannelScheduleForDay> {
        val startDate = start.toLocalDateTime(timeZone).date
        val endDate = startDate - pastRange

        TODO("not implemented")
    }

    private suspend fun loadFuture(
        loadSize: Int,
        pagination: Pagination.Future,
    ): LoadResult<Pagination, ChannelScheduleForDay> {
        val startDate = start.toLocalDateTime(timeZone).date
        val endDate = startDate + futureRange

        val lastTimeSlotOfPreviousPage: LocalDate =
            pagination.lastTimeSlotOfPreviousPage
                ?.toLocalDateTime(timeZone)?.date
                ?: startDate

        return twitchClient
            .getChannelSchedule(
                channelId = channelId,
                start = start,
                limit = loadSize,
                after = pagination.cursor,
            )
            .fold(
                onSuccess = { response ->
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

                    val lastDateOnPage: LocalDate? = groupedData.maxOfOrNull { it.key }

                    val nextKey: Pagination.Future? =
                        response.pagination.cursor
                            ?.takeIf { lastDateOnPage != null && lastDateOnPage < endDate }
                            ?.let { cursor ->
                                Pagination.Future(
                                    cursor = cursor,
                                    lastTimeSlotOfPreviousPage = data.lastOrNull()?.endTime,
                                )
                            }

                    // Either this will be the start date for the last item in the page,
                    // or if there is no next page, or we are going out of the range, then
                    // this will be the end date.
                    val lastTimeSlotOfPage: LocalDate =
                        lastDateOnPage?.takeIf { nextKey != null } ?: endDate

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
                                segments = groupedData[date]
                                    .orEmpty()
                                    .toPersistentList(),
                            )
                        },
                        prevKey = null,
                        nextKey = nextKey,
                        itemsAfter = if (nextKey != null) {
                            LoadResult.Page.COUNT_UNDEFINED
                        } else {
                            0
                        },
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
                                segments = persistentListOf(),
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
