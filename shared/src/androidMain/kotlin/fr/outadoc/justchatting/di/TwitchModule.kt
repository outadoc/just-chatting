package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.feature.auth.data.TwitchAuthApi
import fr.outadoc.justchatting.feature.auth.domain.AuthApi
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesApi
import fr.outadoc.justchatting.feature.chat.data.irc.recent.RecentMessagesServer
import fr.outadoc.justchatting.feature.emotes.data.bttv.BttvEmotesApi
import fr.outadoc.justchatting.feature.emotes.data.bttv.BttvEmotesServer
import fr.outadoc.justchatting.feature.emotes.data.stv.StvEmotesApi
import fr.outadoc.justchatting.feature.emotes.data.stv.StvEmotesServer
import fr.outadoc.justchatting.feature.home.data.TwitchApiImpl
import fr.outadoc.justchatting.feature.home.data.TwitchClient
import fr.outadoc.justchatting.feature.home.domain.TwitchApi
import fr.outadoc.justchatting.feature.home.domain.TwitchRepository
import fr.outadoc.justchatting.feature.home.domain.TwitchRepositoryImpl
import fr.outadoc.justchatting.utils.core.DefaultJson
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

public val twitchModule: Module = module {
    single<Json> { DefaultJson }

    single {
        OAuthAppCredentials(
            clientId = "l9klwmh97qgn0s0me276ezsft5szp2",
            redirectUri = "https://just-chatting.app/auth/callback.html",
        )
    }

    single<TwitchRepository> { TwitchRepositoryImpl(get(), get(), get(), get()) }
    single<TwitchApi> { TwitchApiImpl(get()) }
    single { TwitchClient(get(named("twitch"))) }

    single<AuthApi> { TwitchAuthApi(get(named("twitch"))) }
    single<BttvEmotesApi> { BttvEmotesServer(get()) }
    single<StvEmotesApi> { StvEmotesServer(get()) }
    single<RecentMessagesApi> { RecentMessagesServer(get()) }
}
