package fr.outadoc.justchatting.feature.timeline.presentation

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.PagingData
import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.chat.domain.model.TwitchBadge
import fr.outadoc.justchatting.feature.emotes.domain.model.Emote
import fr.outadoc.justchatting.feature.followed.domain.model.ChannelFollow
import fr.outadoc.justchatting.feature.preferences.domain.AuthRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.ApiToken
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.feature.search.domain.model.ChannelSearchResult
import fr.outadoc.justchatting.feature.shared.domain.TwitchRepository
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.feature.timeline.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.feature.timeline.domain.model.DaySchedule
import fr.outadoc.justchatting.feature.timeline.domain.model.FullSchedule
import fr.outadoc.justchatting.feature.timeline.domain.model.Stream
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.TimeZoneProvider
import fr.outadoc.justchatting.utils.datetime.JCLocalDate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
internal class FutureTimelineViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testClock =
        object : Clock {
            var now: Instant = Instant.parse("2022-01-01T18:00:00Z")

            override fun now(): Instant = now
        }

    /** The time zone the view model sees. Tests can change it, like the user could. */
    private val timeZoneProvider = FakeTimeZoneProvider(TimeZone.UTC)

    private val loggedInAppUser =
        AppUser.LoggedIn(
            userId = "app-user-id",
            userLogin = "appuser",
            token = ApiToken("valid-token"),
        )

    private val user =
        User(
            id = "channel-id",
            login = "channelname",
            displayName = "ChannelName",
            description = "",
            profileImageUrl = "",
            createdAt = Instant.DISTANT_PAST,
            usedAt = null,
        )

    private lateinit var twitchRepository: FakeTwitchRepository
    private lateinit var viewModel: FutureTimelineViewModel

    private val viewModelStore = ViewModelStore()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        twitchRepository = FakeTwitchRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Runs [block] as a test, then clears the view model before [runTest] waits for the
     * scheduler to go idle. The view model updates its state every minute forever, so the
     * scheduler would otherwise never be idle and the test would never end.
     */
    private fun runViewModelTest(block: suspend TestScope.() -> Unit) =
        runTest(testDispatcher) {
            try {
                block()
            } finally {
                viewModelStore.clear()
            }
        }

    private fun createViewModel(): FutureTimelineViewModel =
        ViewModelProvider
            .create(
                store = viewModelStore,
                factory =
                    viewModelFactory {
                        initializer {
                            FutureTimelineViewModel(
                                twitchRepository = twitchRepository,
                                clock = testClock,
                                authRepository = FakeAuthRepository(loggedInAppUser),
                                dispatchersProvider = TestDispatchersProvider(testDispatcher),
                                timeZoneProvider = timeZoneProvider,
                            )
                        }
                    },
            )[FutureTimelineViewModel::class]

    private fun segment(
        id: String,
        start: String,
        end: String,
    ) = ChannelScheduleSegment(
        id = id,
        user = user,
        startTime = Instant.parse(start),
        endTime = Instant.parse(end),
        title = "",
    )

    private fun schedule(vararg days: DaySchedule) = FullSchedule(future = persistentListOf(*days))

    private fun day(
        date: LocalDate,
        vararg segments: ChannelScheduleSegment,
    ) = DaySchedule(
        date = JCLocalDate(date),
        schedule = segments.toList(),
    )

    /** Comparable view of a day: [JCLocalDate] doesn't implement equality. */
    private data class DaySummary(
        val daysFromToday: Int,
        val ongoing: List<String>,
        val upcoming: List<String>,
    )

    private val FutureTimelineViewModel.daySummaries: List<DaySummary>
        get() =
            state.value.days.map { day ->
                DaySummary(
                    daysFromToday = day.daysFromToday,
                    ongoing = day.ongoing.map { it.segment.id },
                    upcoming = day.upcoming.map { it.id },
                )
            }

    @Test
    fun `the followed channels' schedule is split into what's on now and what's to come`() =
        runViewModelTest {
            twitchRepository.schedule.value =
                schedule(
                    day(
                        LocalDate(2022, 1, 1),
                        segment("ended", start = "2022-01-01T12:00:00Z", end = "2022-01-01T14:00:00Z"),
                        segment("ongoing", start = "2022-01-01T17:00:00Z", end = "2022-01-01T19:00:00Z"),
                        segment("upcoming", start = "2022-01-01T20:00:00Z", end = "2022-01-01T22:00:00Z"),
                    ),
                    day(
                        LocalDate(2022, 1, 2),
                        segment("tomorrow", start = "2022-01-02T12:00:00Z", end = "2022-01-02T14:00:00Z"),
                    ),
                )

            viewModel = createViewModel()

            assertEquals(
                listOf(
                    DaySummary(daysFromToday = 0, ongoing = listOf("ongoing"), upcoming = listOf("upcoming")),
                    DaySummary(daysFromToday = 1, ongoing = emptyList(), upcoming = listOf("tomorrow")),
                ),
                viewModel.daySummaries,
            )
        }

    @Test
    fun `the schedule is read from today, in the current time zone`() =
        runViewModelTest {
            timeZoneProvider.currentTimeZone = TimeZone.of("Asia/Tokyo")

            viewModel = createViewModel()

            // 18:00 UTC is already the next day in Tokyo.
            assertEquals(
                listOf(LocalDate(2022, 1, 2) to TimeZone.of("Asia/Tokyo")),
                twitchRepository.scheduleRequests,
            )
        }

    @Test
    fun `days are updated when the schedule changes`() =
        runViewModelTest {
            viewModel = createViewModel()
            assertEquals(emptyList(), viewModel.daySummaries)

            twitchRepository.schedule.value =
                schedule(
                    day(
                        LocalDate(2022, 1, 1),
                        segment("upcoming", start = "2022-01-01T20:00:00Z", end = "2022-01-01T22:00:00Z"),
                    ),
                )

            assertEquals(
                listOf(DaySummary(daysFromToday = 0, ongoing = emptyList(), upcoming = listOf("upcoming"))),
                viewModel.daySummaries,
            )
        }

    @Test
    fun `what's on now is updated every minute`() =
        runViewModelTest {
            testClock.now = Instant.parse("2022-01-01T17:59:30Z")
            twitchRepository.schedule.value =
                schedule(
                    day(
                        LocalDate(2022, 1, 1),
                        segment("segment", start = "2022-01-01T18:00:00Z", end = "2022-01-01T20:00:00Z"),
                    ),
                )

            viewModel = createViewModel()
            assertEquals(
                listOf(DaySummary(daysFromToday = 0, ongoing = emptyList(), upcoming = listOf("segment"))),
                viewModel.daySummaries,
            )

            testClock.now = Instant.parse("2022-01-01T18:00:30Z")
            testDispatcher.scheduler.advanceTimeBy(1.minutes)
            testDispatcher.scheduler.runCurrent()

            assertEquals(
                listOf(DaySummary(daysFromToday = 0, ongoing = listOf("segment"), upcoming = emptyList())),
                viewModel.daySummaries,
            )
        }

    @Test
    fun `a time zone change is taken into account on the next update`() =
        runViewModelTest {
            testClock.now = Instant.parse("2022-01-01T23:30:00Z")
            twitchRepository.schedule.value =
                schedule(
                    day(
                        LocalDate(2022, 1, 2),
                        segment("segment", start = "2022-01-02T12:00:00Z", end = "2022-01-02T14:00:00Z"),
                    ),
                )

            viewModel = createViewModel()
            assertEquals(listOf(1), viewModel.daySummaries.map { it.daysFromToday })

            // It's already January 2nd in Tokyo, so the segment's day becomes today.
            timeZoneProvider.currentTimeZone = TimeZone.of("Asia/Tokyo")
            testDispatcher.scheduler.advanceTimeBy(1.minutes)
            testDispatcher.scheduler.runCurrent()

            assertEquals(listOf(0), viewModel.daySummaries.map { it.daysFromToday })
        }

    @Test
    fun `syncing the schedule shows a loading state until it's done`() =
        runViewModelTest {
            val syncGate = CompletableDeferred<Unit>()
            twitchRepository.scheduleSyncGate = syncGate

            viewModel = createViewModel()
            assertFalse(viewModel.state.value.isLoading)

            viewModel.syncEverythingNow()
            assertTrue(viewModel.state.value.isLoading)

            syncGate.complete(Unit)
            assertFalse(viewModel.state.value.isLoading)
        }

    @Test
    fun `the schedule is synced from today, in the current time zone, for the current user`() =
        runViewModelTest {
            viewModel = createViewModel()

            viewModel.syncEverythingNow()

            assertEquals(
                listOf(
                    ScheduleSync(
                        today = LocalDate(2022, 1, 1),
                        timeZone = TimeZone.UTC,
                        appUser = loggedInAppUser,
                    ),
                ),
                twitchRepository.scheduleSyncs,
            )
        }

    @Test
    fun `live streams are synced for the current user`() =
        runViewModelTest {
            viewModel = createViewModel()

            viewModel.syncLiveStreamsNow()

            assertEquals(listOf<AppUser>(loggedInAppUser), twitchRepository.streamSyncs)
        }
}

private data class ScheduleSync(
    val today: LocalDate,
    val timeZone: TimeZone,
    val appUser: AppUser,
)

private class FakeTwitchRepository : TwitchRepository {
    val schedule = MutableStateFlow(FullSchedule())

    /** The day and time zone of every [getFollowedChannelsSchedule] call. */
    val scheduleRequests = mutableListOf<Pair<LocalDate, TimeZone>>()
    val scheduleSyncs = mutableListOf<ScheduleSync>()
    val streamSyncs = mutableListOf<AppUser>()

    /** When set, [syncFollowedChannelsSchedule] doesn't return until this is completed. */
    var scheduleSyncGate: CompletableDeferred<Unit>? = null

    override suspend fun getFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
    ): Flow<FullSchedule> {
        scheduleRequests += today to timeZone
        return schedule
    }

    override suspend fun syncFollowedChannelsSchedule(
        today: LocalDate,
        timeZone: TimeZone,
        appUser: AppUser,
    ) {
        scheduleSyncs += ScheduleSync(today = today, timeZone = timeZone, appUser = appUser)
        scheduleSyncGate?.await()
    }

    override suspend fun syncFollowedStreams(appUser: AppUser) {
        streamSyncs += appUser
    }

    override suspend fun searchChannels(query: String): Flow<PagingData<ChannelSearchResult>> = error("Not used in tests")

    override suspend fun getFollowedChannels(): Flow<List<ChannelFollow>> = error("Not used in tests")

    override suspend fun getStreamByUserId(userId: String): Flow<Result<Stream>> = error("Not used in tests")

    override suspend fun getUserById(id: String): Flow<Result<User>> = error("Not used in tests")

    override suspend fun getUsersById(ids: List<String>): Flow<Result<List<User>>> = error("Not used in tests")

    override suspend fun getCheerEmotes(userId: String): Result<List<Emote>> = error("Not used in tests")

    override suspend fun getEmotesFromSet(setIds: List<String>): Result<List<Emote>> = error("Not used in tests")

    override suspend fun getRecentChannels(): Flow<List<User>> = error("Not used in tests")

    override suspend fun forgetRecentChannel(userId: String) = error("Not used in tests")

    override suspend fun markChannelAsVisited(
        userId: String,
        visitedAt: Instant,
    ) = error("Not used in tests")

    override suspend fun getGlobalBadges(): Result<List<TwitchBadge>> = error("Not used in tests")

    override suspend fun getChannelBadges(channelId: String): Result<List<TwitchBadge>> = error("Not used in tests")

    override suspend fun sendChatMessage(
        channelUserId: String,
        message: String,
        inReplyToMessageId: String?,
        appUser: AppUser,
    ): Result<String> = error("Not used in tests")

    override suspend fun syncFollowedChannels(appUser: AppUser) = error("Not used in tests")
}

private class FakeAuthRepository(
    appUser: AppUser,
) : AuthRepository {
    override val currentUser: Flow<AppUser> = flowOf(appUser)

    override suspend fun saveToken(token: ApiToken) = error("Not used in tests")

    override suspend fun logout() = error("Not used in tests")

    override fun getExternalAuthorizeUrl(): Uri = error("Not used in tests")
}

private class FakeTimeZoneProvider(
    override var currentTimeZone: TimeZone,
) : TimeZoneProvider

private class TestDispatchersProvider(
    private val dispatcher: CoroutineDispatcher,
) : DispatchersProvider {
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}
