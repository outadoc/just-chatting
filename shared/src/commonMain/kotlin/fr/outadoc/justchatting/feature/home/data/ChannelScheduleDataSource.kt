package fr.outadoc.justchatting.feature.home.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import fr.outadoc.justchatting.feature.home.domain.EpgConfig
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleForDay
import fr.outadoc.justchatting.feature.home.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.home.domain.model.StreamCategory
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

internal class ChannelScheduleDataSource(
    private val channelId: String,
    private val twitchClient: TwitchClient,
    private val clock: Clock,
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

        val tz = TimeZone.currentSystemDefault()
        val today = clock.now().toLocalDateTime(tz).date
        val lastTimeSlotOfPreviousPage: LocalDate =
            pagination?.lastTimeSlotOfPreviousPage?.toLocalDateTime(tz)?.date
                ?: today

        return twitchClient
            .getChannelSchedule(
                channelId = channelId,
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
                        .groupBy { it.startTime.toLocalDateTime(tz).date }

                    val lastTimeSlotOfPage: LocalDate? = groupedData.maxOfOrNull { it.key }

                    val expectedDaysInPage: List<LocalDate> =
                        if (lastTimeSlotOfPage != null) {
                            IntRange(
                                start = lastTimeSlotOfPreviousPage.toEpochDays(),
                                endInclusive = lastTimeSlotOfPage.toEpochDays(),
                            ).map { epochDays ->
                                LocalDate.fromEpochDays(epochDays)
                            }
                        } else {
                            // Pad with empty days if there is no data
                            IntRange(
                                start = lastTimeSlotOfPreviousPage.toEpochDays(),
                                endInclusive = today.plus(EpgConfig.MaxDaysAhead).toEpochDays(),
                            ).map { epochDays ->
                                LocalDate.fromEpochDays(epochDays)
                            }
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
                            start = today.toEpochDays(),
                            endInclusive = today.plus(EpgConfig.MaxDaysAhead).toEpochDays(),
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
