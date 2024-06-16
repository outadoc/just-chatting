package fr.outadoc.justchatting.component.chatapi.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.flatMap
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.chatapi.db.RecentChannelsRepository
import fr.outadoc.justchatting.component.chatapi.db.Recent_channels
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelFollow
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSchedule
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelScheduleSegment
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelScheduleVacation
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearch
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.component.chatapi.domain.repository.datasource.FollowedChannelsDataSource
import fr.outadoc.justchatting.component.chatapi.domain.repository.datasource.FollowedStreamsDataSource
import fr.outadoc.justchatting.component.chatapi.domain.repository.datasource.SearchChannelsDataSource
import fr.outadoc.justchatting.component.preferences.data.AppUser
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.twitch.http.api.HelixApi
import fr.outadoc.justchatting.component.twitch.utils.map
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.logging.logError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlin.jvm.JvmName

class TwitchRepositoryImpl(
    private val helix: HelixApi,
    private val preferencesRepository: PreferenceRepository,
    private val recentChannelsRepository: RecentChannelsRepository,
) : TwitchRepository {

    override suspend fun loadSearchChannels(query: String): Flow<PagingData<ChannelSearch>> {
        val pager = Pager(
            config = PagingConfig(
                pageSize = 15,
                initialLoadSize = 15,
                prefetchDistance = 5,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                SearchChannelsDataSource(
                    query = query,
                    helixApi = helix,
                )
            },
        )

        return pager.flow.map { page ->
            page.flatMap { searchResponse ->
                searchResponse.mapWithUserProfileImages()
            }
        }
    }

    @JvmName("mapWithUserProfileImagesChannelSearch")
    private suspend fun List<ChannelSearch>.mapWithUserProfileImages(): List<ChannelSearch> {
        val results = this
        return results
            .map { result -> result.id }
            .chunked(size = 100)
            .flatMap { idsToUpdate ->
                val users = helix.getUsersById(ids = idsToUpdate)
                    .fold(
                        onSuccess = { response -> response.data },
                        onFailure = { exception ->
                            logError<TwitchRepositoryImpl>(exception) { "Failed to load user profiles" }
                            emptyList()
                        },
                    )

                results.map { searchResult ->
                    searchResult.copy(
                        profileImageUrl = users.firstOrNull { user -> user.id == searchResult.id }
                            ?.profileImageUrl,
                    )
                }
            }
    }

    override suspend fun loadFollowedStreams(): Flow<PagingData<Stream>> {
        val appUser: AppUser = preferencesRepository.currentPreferences.first().appUser

        if (appUser !is AppUser.LoggedIn) return emptyFlow()

        val pager = Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 10,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                FollowedStreamsDataSource(
                    userId = appUser.userId,
                    helixApi = helix,
                )
            },
        )

        return pager.flow.map { page ->
            page.flatMap { streams ->
                streams
                    .associateBy { stream -> stream.userId }
                    .values
                    .mapWithUserProfileImages()
            }
        }
    }

    @JvmName("mapWithUserProfileImagesStream")
    private suspend fun Collection<Stream>.mapWithUserProfileImages(): List<Stream> {
        val results = this
        val users = results
            .map { user -> user.userId }
            .chunked(100)
            .flatMap { ids ->
                helix.getUsersById(ids = ids)
                    .fold(
                        onSuccess = { response -> response.data },
                        onFailure = { exception ->
                            logError<TwitchRepositoryImpl>(exception) { "Failed to load user profiles" }
                            emptyList()
                        },
                    )
            }

        return results.map { stream ->
            val user = users.firstOrNull { user -> stream.userId == user.id }
            stream.copy(
                profileImageURL = user?.profileImageUrl,
            )
        }
    }

    override suspend fun loadFollowedChannels(): Flow<PagingData<ChannelFollow>> {
        val appUser: AppUser = preferencesRepository.currentPreferences.first().appUser

        if (appUser !is AppUser.LoggedIn) return emptyFlow()

        val pager = Pager(
            config = PagingConfig(
                pageSize = 40,
                initialLoadSize = 40,
                prefetchDistance = 10,
                enablePlaceholders = true,
            ),
            pagingSourceFactory = {
                FollowedChannelsDataSource(
                    userId = appUser.userId,
                    helixApi = helix,
                )
            },
        )

        return pager.flow.map { page ->
            page.flatMap { follows ->
                follows.mapWithUserProfileImages()
            }
        }
    }

    @JvmName("mapWithUserProfileImagesChannelFollow")
    private suspend fun Collection<ChannelFollow>.mapWithUserProfileImages(): Collection<ChannelFollow> {
        val results = this
        val userMap: Map<String, User> = results
            .filter { follow -> follow.profileImageURL == null }
            .map { follow -> follow.userId }
            .chunked(size = 100)
            .flatMap { idsToUpdate ->
                helix.getUsersById(ids = idsToUpdate)
                    .fold(
                        onSuccess = { response -> response.data },
                        onFailure = { exception ->
                            logError<TwitchRepositoryImpl>(exception) { "Failed to load user profiles" }
                            emptyList()
                        },
                    )
            }
            .associate { user ->
                user.id to User(
                    id = user.id,
                    login = user.login,
                    displayName = user.displayName,
                    description = user.description,
                    profileImageUrl = user.profileImageUrl,
                    createdAt = user.createdAt,
                )
            }

        return results.map { follow ->
            follow.copy(
                profileImageURL = userMap[follow.userId]?.profileImageUrl,
            )
        }
    }

    override suspend fun loadStream(userId: String): Result<Stream> =
        withContext(DispatchersProvider.io) {
            helix.getStreams(ids = listOf(userId))
                .mapCatching { response ->
                    val stream = response.data.firstOrNull()
                        ?: error("Stream for userId $userId not found")

                    Stream(
                        id = stream.id,
                        userId = stream.userId,
                        userLogin = stream.userLogin,
                        userName = stream.userName,
                        gameName = stream.gameName,
                        title = stream.title,
                        viewerCount = stream.viewerCount,
                        startedAt = stream.startedAt,
                        tags = stream.tags,
                    )
                }
        }

    override suspend fun loadUsersById(ids: List<String>): Result<List<User>> =
        withContext(DispatchersProvider.io) {
            helix.getUsersById(ids = ids)
                .map { response ->
                    response.data.map { user ->
                        User(
                            id = user.id,
                            login = user.login,
                            displayName = user.displayName,
                            description = user.description,
                            profileImageUrl = user.profileImageUrl,
                            createdAt = user.createdAt,
                        )
                    }
                }
        }

    override suspend fun loadUsersByLogin(logins: List<String>): Result<List<User>> =
        withContext(DispatchersProvider.io) {
            helix.getUsersByLogin(logins = logins)
                .mapCatching { response ->
                    if (response.data.isEmpty()) {
                        error("No users found for logins: $logins")
                    }

                    response.data.map { user ->
                        User(
                            id = user.id,
                            login = user.login,
                            displayName = user.displayName,
                            description = user.description,
                            profileImageUrl = user.profileImageUrl,
                            createdAt = user.createdAt,
                        )
                    }
                }
        }

    override suspend fun loadUserByLogin(login: String): Result<User> {
        return loadUsersByLogin(logins = listOf(login))
            .mapCatching { users ->
                if (users.isEmpty()) {
                    error("No user found for login: $login")
                }

                users.first()
            }
    }

    override suspend fun loadCheerEmotes(userId: String): Result<List<Emote>> {
        return withContext(DispatchersProvider.io) {
            helix.getCheerEmotes(userId = userId)
                .map { response ->
                    response.data.flatMap { emote ->
                        emote.tiers.map { tier ->
                            tier.map(prefix = emote.prefix)
                        }
                    }
                }
        }
    }

    override suspend fun loadEmotesFromSet(setIds: List<String>): Result<List<Emote>> =
        withContext(DispatchersProvider.io) {
            helix.getEmotesFromSet(setIds = setIds)
                .map { response ->
                    response.data
                        .sortedByDescending { it.setId }
                        .map { emote -> emote.map(templateUrl = response.template) }
                }
        }

    override suspend fun getRecentChannels(): Flow<List<ChannelSearch>?> {
        return withContext(DispatchersProvider.io) {
            recentChannelsRepository.getAll()
                .map { channels ->
                    val ids = channels.map { channel -> channel.id }
                    helix.getUsersById(ids = ids)
                        .getOrNull()
                        ?.data
                        ?.map { user ->
                            ChannelSearch(
                                id = user.id,
                                title = user.displayName,
                                broadcasterLogin = user.login,
                                broadcasterDisplayName = user.displayName,
                                profileImageUrl = user.profileImageUrl,
                            )
                        }
                        ?.sortedBy { user ->
                            ids.indexOf(user.id)
                        }
                }
        }
    }

    override suspend fun insertRecentChannel(channel: User, usedAt: Instant) {
        withContext(DispatchersProvider.io) {
            recentChannelsRepository.insert(
                Recent_channels(
                    id = channel.id,
                    used_at = usedAt.toEpochMilliseconds(),
                ),
            )
        }
    }

    override suspend fun loadChannelSchedule(channelId: String): Result<ChannelSchedule> {
        return withContext(DispatchersProvider.io) {
            helix.getChannelSchedule(
                channelId = channelId,
                limit = 10,
                after = null,
            ).mapCatching { response ->
                ChannelSchedule(
                    segments = response.data.segments.map { segment ->
                        ChannelScheduleSegment(
                            id = segment.id,
                            startTime = segment.startTime,
                            endTime = segment.endTime,
                            title = segment.title,
                            canceledUntil = segment.canceledUntil,
                            category = segment.category,
                            isRecurring = segment.isRecurring,
                        )
                    },
                    userId = response.data.userId,
                    userLogin = response.data.userLogin,
                    userDisplayName = response.data.userDisplayName,
                    vacation = response.data.vacation?.let { vacation ->
                        ChannelScheduleVacation(
                            startTime = vacation.startTime,
                            endTime = vacation.endTime,
                        )
                    },
                )
            }
        }
    }
}
