package fr.outadoc.justchatting.auth

import fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository
import fr.outadoc.justchatting.component.twitch.api.TwitchAuth
import fr.outadoc.justchatting.component.twitch.model.OAuthAppCredentials
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Invocation

class AuthenticationInterceptor(
    private val preferenceRepository: fr.outadoc.justchatting.component.preferences.domain.PreferenceRepository,
    private val oAuthAppCredentials: OAuthAppCredentials
) : Interceptor {

    companion object {
        private const val TWITCH_HEADER_CLIENT_ID = "Client-ID"
        private const val TWITCH_HEADER_AUTHORIZATION = "Authorization"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!request.isMarkedRequest) return chain.proceed(request)

        return runBlocking {
            val prefs = preferenceRepository.currentPreferences.first()
            val authenticatedRequest: Request =
                request.newBuilder()
                    .header(TWITCH_HEADER_CLIENT_ID, oAuthAppCredentials.clientId)
                    .apply {
                        prefs.appUser.helixToken?.let { token ->
                            header(TWITCH_HEADER_AUTHORIZATION, "Bearer $token")
                        }
                    }
                    .build()

            chain.proceed(authenticatedRequest)
        }
    }

    private val Request.isMarkedRequest: Boolean
        get() = getAnnotation(TwitchAuth::class.java) != null

    private fun <T : Annotation> Request.getAnnotation(annotationClass: Class<T>): T? =
        this.tag(Invocation::class.java)?.method()?.getAnnotation(annotationClass)
}
