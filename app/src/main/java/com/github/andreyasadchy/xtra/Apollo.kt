package com.github.andreyasadchy.xtra

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

private var instance: ApolloClient? = null

fun apolloClient(clientId: String?): ApolloClient {
    if (instance != null) {
        return instance!!
    }

    instance = ApolloClient.Builder()
        .serverUrl("https://gql.twitch.tv/gql/")
        .okHttpClient(OkHttpClient.Builder()
            .addInterceptor(AuthorizationInterceptor(clientId))
            .build()
        )
        .build()

    return instance!!
}

private class AuthorizationInterceptor(val clientId: String?): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Client-ID", clientId ?: "")
            .build()

        return chain.proceed(request)
    }
}
