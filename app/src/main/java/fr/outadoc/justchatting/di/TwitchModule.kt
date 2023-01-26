package fr.outadoc.justchatting.di

import androidx.core.net.toUri
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import fr.outadoc.justchatting.component.chatapi.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.component.chatapi.domain.repository.EmotesRepository
import fr.outadoc.justchatting.component.chatapi.domain.repository.TwitchRepository
import fr.outadoc.justchatting.component.chatapi.domain.repository.TwitchRepositoryImpl
import fr.outadoc.justchatting.component.twitch.api.BttvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.HelixApi
import fr.outadoc.justchatting.component.twitch.api.IdApi
import fr.outadoc.justchatting.component.twitch.api.StvEmotesApi
import fr.outadoc.justchatting.component.twitch.api.TwitchBadgesApi
import fr.outadoc.justchatting.feature.chat.data.recent.RecentMessagesApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit

val twitchModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
        }
    }

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

@OptIn(ExperimentalSerializationApi::class)
private inline fun <reified T> Scope.createApi(baseUrl: String): T {
    val contentType = "application/json".toMediaType()
    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(get())
        .addConverterFactory(get<Json>().asConverterFactory(contentType))
        .build()
        .create(T::class.java)
}
