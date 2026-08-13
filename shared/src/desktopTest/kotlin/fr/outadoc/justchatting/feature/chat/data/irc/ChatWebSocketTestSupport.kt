package fr.outadoc.justchatting.feature.chat.data.irc

import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesApi
import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesResponse
import fr.outadoc.justchatting.feature.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import fr.outadoc.justchatting.utils.core.NetworkStateObserver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

internal val testClock =
    object : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }

internal val testAppUser =
    AppUser.LoggedIn(
        userId = "app-user-id",
        userLogin = "appuser",
        token = "valid-token",
    )

internal const val TEST_CHANNEL_ID = "channel-id"
internal const val TEST_CHANNEL_LOGIN = "channelname"

internal const val SAMPLE_PRIVMSG =
    "@badge-info=;badges=turbo/1;color=#0D4200;display-name=ronni;emotes=;id=b34ccfc7-4977-403a-8a94-33c6bac34fb8;mod=0;room-id=1337;subscriber=0;tmi-sent-ts=1507246572675;turbo=1;user-id=1337;user-type=global_mod :ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #channelname :Kappa Keepo Kappa"

internal fun privMsg(
    text: String,
    timestamp: Long,
): String =
    "@badge-info=;badges=;color=#0D4200;display-name=ronni;emotes=;id=msg-$timestamp;mod=0;room-id=1337;subscriber=0;tmi-sent-ts=$timestamp;turbo=0;user-id=1337;user-type= :ronni!ronni@ronni.tmi.twitch.tv PRIVMSG #$TEST_CHANNEL_LOGIN :$text"

internal const val SAMPLE_NOTICE =
    "@msg-id=msg_ratelimit :tmi.twitch.tv NOTICE #channelname :Your message was not sent because you are sending messages too quickly."

internal const val SAMPLE_USERSTATE =
    "@badge-info=;badges=;color=;display-name=AppUser;emote-sets=0,33,50;mod=0;subscriber=0;user-type= :tmi.twitch.tv USERSTATE #channelname"

internal class RealDispatchersProvider : DispatchersProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val default: CoroutineDispatcher = Dispatchers.Default
}

internal class FakeNetworkStateObserver(
    initialState: NetworkStateObserver.NetworkState = NetworkStateObserver.NetworkState.Available,
) : NetworkStateObserver {
    val mutableState = MutableStateFlow(initialState)
    override val state: Flow<NetworkStateObserver.NetworkState> = mutableState
}

internal class FakeRecentMessagesApi(
    var messages: List<String> = emptyList(),
) : RecentMessagesApi {
    var responseDelay: Duration = Duration.ZERO

    override suspend fun getRecentMessages(
        channelLogin: String,
        limit: Int,
    ): Result<RecentMessagesResponse> {
        delay(responseDelay)
        return Result.success(RecentMessagesResponse(messages))
    }
}

internal class FakePreferenceRepository(
    initialPreferences: AppPreferences = AppPreferences(),
) : PreferenceRepository {
    private val preferences = MutableStateFlow(initialPreferences)

    override val currentPreferences: Flow<AppPreferences> = preferences

    override suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences) {
        preferences.update(update)
    }
}
