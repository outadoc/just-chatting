package fr.outadoc.justchatting.di

import androidx.core.net.toUri
import fr.outadoc.justchatting.component.twitch.api.BttvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.HelixApi
import fr.outadoc.justchatting.component.twitch.api.IdApi
import fr.outadoc.justchatting.component.twitch.api.StvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.TwitchBadgesApi
import fr.outadoc.justchatting.component.twitch.domain.api.TwitchRepository
import fr.outadoc.justchatting.component.twitch.domain.repository.EmotesRepository
import fr.outadoc.justchatting.component.twitch.domain.repository.TwitchRepositoryImpl
import fr.outadoc.justchatting.component.twitch.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.chat.data.recent.RecentMessagesApi
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val twitchModule = module {
    single {
        OAuthAppCredentials(
            clientId = "l9klwmh97qgn0s0me276ezsft5szp2",
            redirectUri = "https://just-chatting.app/auth/callback.html".toUri()
        )
    }

    single { EmotesRepository(get(), get(), get(), get()) }

    single<TwitchRepository> { TwitchRepositoryImpl(get(), get()) }

    single<HelixApi> { createApi("https://api.twitch.tv/helix/") }
    single<TwitchBadgesApi> { createApi("https://badges.twitch.tv/") }
    single<BttvEmotesApi> { createApi("https://api.betterttv.net/") }
    single<StvEmotesApi> { createApi("https://api.7tv.app/") }
    single<RecentMessagesApi> { createApi("https://recent-messages.robotty.de/api/") }
    single<IdApi> { createApi("https://id.twitch.tv/oauth2/") }
}

private inline fun <reified T> Scope.createApi(baseUrl: String): T =
    Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(get())
        .addConverterFactory(get<GsonConverterFactory>())
        .build()
        .create(T::class.java)
