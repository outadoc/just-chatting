package fr.outadoc.justchatting.di

import androidx.core.net.toUri
import fr.outadoc.justchatting.component.chatapi.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.component.chatapi.domain.repository.EmotesRepository
import fr.outadoc.justchatting.component.chatapi.domain.repository.TwitchRepository
import fr.outadoc.justchatting.component.chatapi.domain.repository.TwitchRepositoryImpl
import fr.outadoc.justchatting.component.twitch.api.BttvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.HelixApi
import fr.outadoc.justchatting.component.twitch.api.IdApi
import fr.outadoc.justchatting.component.twitch.api.StvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.TwitchBadgesApi
import fr.outadoc.justchatting.component.twitch.server.BttvEmotesServer
import fr.outadoc.justchatting.component.twitch.server.HelixServer
import fr.outadoc.justchatting.component.twitch.server.IdServer
import fr.outadoc.justchatting.component.twitch.server.StvEmotesServer
import fr.outadoc.justchatting.component.twitch.server.TwitchBadgesServer
import fr.outadoc.justchatting.feature.chat.data.recent.RecentMessagesApi
import fr.outadoc.justchatting.feature.chat.data.recent.RecentMessagesServer
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val twitchModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }

    single {
        OAuthAppCredentials(
            clientId = "l9klwmh97qgn0s0me276ezsft5szp2",
            redirectUri = "https://just-chatting.app/auth/callback.html".toUri(),
        )
    }

    single { EmotesRepository(get(), get(), get(), get()) }

    single<TwitchRepository> { TwitchRepositoryImpl(get(), get()) }

    single<IdApi> { IdServer(get(named("twitch"))) }
    single<HelixApi> { HelixServer(get(named("twitch"))) }
    single<TwitchBadgesApi> { TwitchBadgesServer(get()) }
    single<BttvEmotesApi> { BttvEmotesServer(get()) }
    single<StvEmotesApi> { StvEmotesServer(get()) }
    single<RecentMessagesApi> { RecentMessagesServer(get()) }
}
