package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.component.twitch.websocket.irc.recent.RecentMessagesApi
import fr.outadoc.justchatting.component.twitch.websocket.irc.recent.RecentMessagesServer
import fr.outadoc.justchatting.feature.auth.data.IdApi
import fr.outadoc.justchatting.feature.auth.data.IdServer
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.emotes.data.bttv.BttvEmotesApi
import fr.outadoc.justchatting.feature.emotes.data.bttv.BttvEmotesServer
import fr.outadoc.justchatting.feature.emotes.data.stv.StvEmotesApi
import fr.outadoc.justchatting.feature.emotes.data.stv.StvEmotesServer
import fr.outadoc.justchatting.feature.emotes.domain.EmotesRepository
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

    single { EmotesRepository(get(), get(), get(), get(), get()) }

    single<TwitchRepository> { TwitchRepositoryImpl(get(), get(), get()) }

    single<IdApi> { IdServer(get(named("twitch"))) }
    single<fr.outadoc.justchatting.feature.home.data.TwitchApi> {
        fr.outadoc.justchatting.feature.home.data.TwitchServer(
            get(named("twitch")),
        )
    }
    single<BttvEmotesApi> { BttvEmotesServer(get()) }
    single<StvEmotesApi> { StvEmotesServer(get()) }
    single<RecentMessagesApi> { RecentMessagesServer(get()) }
}
