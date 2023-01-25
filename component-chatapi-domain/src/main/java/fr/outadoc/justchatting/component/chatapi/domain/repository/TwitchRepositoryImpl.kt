package fr.outadoc.justchatting.component.chatapi.domain.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearch
import fr.outadoc.justchatting.component.chatapi.domain.model.ChannelSearchResponse
import fr.outadoc.justchatting.component.chatapi.domain.model.CheerEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.Follow
import fr.outadoc.justchatting.component.chatapi.domain.model.FollowResponse
import fr.outadoc.justchatting.component.chatapi.domain.model.Stream
import fr.outadoc.justchatting.component.chatapi.domain.model.StreamsResponse
import fr.outadoc.justchatting.component.chatapi.domain.model.TwitchEmote
import fr.outadoc.justchatting.component.chatapi.domain.model.User
import fr.outadoc.justchatting.component.chatapi.domain.repository.datasource.FollowedChannelsDataSource
import fr.outadoc.justchatting.component.chatapi.domain.repository.datasource.FollowedStreamsDataSource
import fr.outadoc.justchatting.component.chatapi.domain.repository.datasource.SearchChannelsDataSource
import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.twitch.api.HelixApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class TwitchRepositoryImpl(
    private val helix: HelixApi,
    private val preferencesRepository: PreferenceRepository
) : TwitchRepository {

    override suspend fun loadSearchChannels(query: String): Pager<String, ChannelSearchResponse> {
        return Pager(
            config = PagingConfig(
                pageSize = 15,
                initialLoadSize = 15,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                SearchChannelsDataSource(
                    query = query,
                    helixApi = helix
                )
            }
        )
    }

    override suspend fun mapSearchWithUserProfileImages(searchResults: List<ChannelSearch>): List<ChannelSearch> =
        with(searchResults) {
            return mapNotNull { result -> result.id }
                .chunked(size = 100)
                .flatMap { idsToUpdate ->
                    val users = helix.getUsersById(ids = idsToUpdate)
                        .data
                        .orEmpty()

                    map { searchResult ->
                        searchResult.copy(
                            profileImageURL = users.firstOrNull { user -> user.id == searchResult.id }
                                ?.profileImageUrl
                        )
                    }
                }
        }

    override suspend fun loadFollowedStreams(): Pager<String, StreamsResponse> {
        val prefs = preferencesRepository.currentPreferences.first()
        return Pager(
            config = PagingConfig(
                pageSize = 30,
                initialLoadSize = 30,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                FollowedStreamsDataSource(
                    userId = prefs.appUser.id,
                    helixApi = helix
                )
            }
        )
    }

    override suspend fun mapStreamsWithUserProfileImages(streams: Collection<Stream>): List<Stream> =
        with(streams) {
            val users = mapNotNull { it.userId }
                .chunked(100)
                .flatMap { ids ->
                    helix.getUsersById(ids = ids)
                        .data
                        .orEmpty()
                }

            return map { stream ->
                val user = users.firstOrNull { user -> stream.userId == user.id }
                stream.copy(
                    profileImageURL = user?.profileImageUrl
                )
            }
        }

    override suspend fun loadFollowedChannels(): Pager<String, FollowResponse> {
        val prefs = preferencesRepository.currentPreferences.first()
        return Pager(
            config = PagingConfig(
                pageSize = 40,
                initialLoadSize = 40,
                prefetchDistance = 10,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                FollowedChannelsDataSource(
                    userId = prefs.appUser.id,
                    helixApi = helix
                )
            }
        )
    }

    override suspend fun mapFollowsWithUserProfileImages(follows: Collection<Follow>): Collection<Follow> =
        with(follows) {
            val results: List<User> =
                filter { follow -> follow.profileImageURL == null }
                    .mapNotNull { follow -> follow.toId }
                    .chunked(size = 100)
                    .flatMap { idsToUpdate ->
                        helix.getUsersById(ids = idsToUpdate)
                            .data
                            .orEmpty()
                    }
                    .map { user ->
                        User(
                            id = user.id,
                            login = user.login,
                            displayName = user.displayName,
                            description = user.description,
                            profileImageUrl = user.profileImageUrl,
                            offlineImageUrl = user.offlineImageUrl,
                            createdAt = user.createdAt,
                            followersCount = user.followersCount
                        )
                    }

            return map { follow ->
                val userInfo = results.firstOrNull { user -> user.id == follow.toId }
                follow.copy(
                    profileImageURL = userInfo?.profileImageUrl
                )
            }
        }

    override suspend fun loadStreamWithUser(channelId: String): Stream? =
        withContext(Dispatchers.IO) {
            helix.getStreams(ids = listOf(channelId))
                .data
                ?.firstOrNull()
                ?.let { stream ->
                    Stream(
                        id = stream.id,
                        userId = stream.userId,
                        userLogin = stream.userLogin,
                        userName = stream.userName,
                        gameId = stream.gameId,
                        gameName = stream.gameName,
                        type = stream.type,
                        title = stream.title,
                        viewerCount = stream.viewerCount,
                        startedAt = stream.startedAt,
                        profileImageURL = stream.profileImageURL
                    )
                }
        }

    override suspend fun loadUsersById(ids: List<String>): List<User>? =
        withContext(Dispatchers.IO) {
            helix.getUsersById(ids = ids).data?.map { user ->
                User(
                    id = user.id,
                    login = user.login,
                    displayName = user.displayName,
                    description = user.description,
                    profileImageUrl = user.profileImageUrl,
                    offlineImageUrl = user.offlineImageUrl,
                    createdAt = user.createdAt,
                    followersCount = user.followersCount
                )
            }
        }

    override suspend fun loadUsersByLogin(logins: List<String>): List<User>? =
        withContext(Dispatchers.IO) {
            helix.getUsersByLogin(logins = logins).data?.map { user ->
                User(
                    id = user.id,
                    login = user.login,
                    displayName = user.displayName,
                    description = user.description,
                    profileImageUrl = user.profileImageUrl,
                    offlineImageUrl = user.offlineImageUrl,
                    createdAt = user.createdAt,
                    followersCount = user.followersCount
                )
            }
        }

    override suspend fun loadCheerEmotes(userId: String): List<CheerEmote> =
        withContext(Dispatchers.IO) {
            helix.getCheerEmotes(userId = userId)
                .data
                .flatMap { emote ->
                    emote.tiers.map { tier ->
                        CheerEmote(
                            name = tier.id,
                            minBits = tier.minBits,
                            color = tier.color,
                            images = tier.images.flatMap { (themeId, theme) ->
                                theme.flatMap { (typeId, urls) ->
                                    urls.map { (scale, url) ->
                                        CheerEmote.Image(
                                            theme = themeId,
                                            isAnimated = typeId == "animated",
                                            dpiScale = scale.toFloat(),
                                            url = url
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
        }

    override suspend fun loadEmotesFromSet(setIds: List<String>): List<TwitchEmote> =
        withContext(Dispatchers.IO) {
            val response = helix.getEmotesFromSet(setIds = setIds)
            response.data
                .map { emote ->
                    TwitchEmote(
                        id = emote.id,
                        name = emote.name,
                        setId = emote.setId,
                        ownerId = emote.ownerId,
                        supportedFormats = emote.format,
                        supportedScales = emote.scale,
                        supportedThemes = emote.themeMode,
                        urlTemplate = response.template
                    )
                }
                .sortedByDescending { it.setId }
        }

    override suspend fun loadUserFollowing(
        userId: String?,
        channelId: String?,
        userLogin: String?
    ): Boolean = withContext(Dispatchers.IO) {
        helix.getUserFollows(
            userId = userId,
            channelId = channelId
        ).total == 1
    }
}
