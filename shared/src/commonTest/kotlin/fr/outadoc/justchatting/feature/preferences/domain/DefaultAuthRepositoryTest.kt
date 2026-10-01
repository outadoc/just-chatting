package fr.outadoc.justchatting.feature.preferences.domain

import com.eygraber.uri.Uri
import fr.outadoc.justchatting.feature.auth.domain.AuthApi
import fr.outadoc.justchatting.feature.auth.domain.model.AuthValidationResponse
import fr.outadoc.justchatting.feature.auth.domain.model.InvalidTokenException
import fr.outadoc.justchatting.feature.auth.domain.model.OAuthAppCredentials
import fr.outadoc.justchatting.feature.preferences.domain.model.ApiToken
import fr.outadoc.justchatting.feature.preferences.domain.model.AppPreferences
import fr.outadoc.justchatting.feature.preferences.domain.model.AppUser
import fr.outadoc.justchatting.utils.core.DispatchersProvider
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultAuthRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @Test
    fun `valid token logs the user in`() =
        runTest(testDispatcher) {
            val repository = createRepository(FakeAuthApi { Result.success(validResponse) })

            assertEquals(
                AppUser.LoggedIn(userId = "app-user-id", userLogin = "appuser", token = token),
                repository.currentUser.first(),
            )
        }

    @Test
    fun `invalid token logs the user out`() =
        runTest(testDispatcher) {
            val repository = createRepository(FakeAuthApi { Result.failure(InvalidTokenException()) })

            assertEquals(AppUser.NotLoggedIn, repository.currentUser.first())
        }

    @Test
    fun `token with missing scopes logs the user out`() =
        runTest(testDispatcher) {
            val repository =
                createRepository(
                    FakeAuthApi {
                        Result.success(
                            AuthValidationResponse(
                                clientId = "client-id",
                                login = "appuser",
                                userId = "app-user-id",
                                scopes = persistentSetOf("chat:read"),
                            ),
                        )
                    },
                )

            assertEquals(AppUser.NotLoggedIn, repository.currentUser.first())
        }

    @Test
    fun `network error reports a validation failure instead of logging out`() =
        runTest(testDispatcher) {
            val repository = createRepository(FakeAuthApi { Result.failure(Exception("Connection timed out")) })

            assertEquals(AppUser.ValidationFailed, repository.currentUser.first())
        }

    @Test
    fun `retrying after a network error validates the token again`() =
        runTest(testDispatcher) {
            var isNetworkUp = false
            val repository =
                createRepository(
                    FakeAuthApi {
                        if (isNetworkUp) {
                            Result.success(validResponse)
                        } else {
                            Result.failure(Exception("Connection timed out"))
                        }
                    },
                )

            val emitted = mutableListOf<AppUser>()
            backgroundScope.launch(testDispatcher) {
                repository.currentUser.collect { emitted += it }
            }
            runCurrent()

            assertEquals(listOf<AppUser>(AppUser.ValidationFailed), emitted)

            repository.retryValidation()
            runCurrent()
            assertEquals(listOf<AppUser>(AppUser.ValidationFailed, AppUser.ValidationFailed), emitted)

            isNetworkUp = true
            repository.retryValidation()
            runCurrent()
            assertEquals(
                AppUser.LoggedIn(userId = "app-user-id", userLogin = "appuser", token = token),
                emitted.last(),
            )
        }

    private fun createRepository(authApi: AuthApi): DefaultAuthRepository =
        DefaultAuthRepository(
            preferenceRepository = FakePreferenceRepository(token),
            authApi = authApi,
            oAuthAppCredentials = credentials,
            dispatchersProvider = TestDispatchersProvider(testDispatcher),
        )

    private companion object {
        val token = ApiToken("token")

        val credentials =
            OAuthAppCredentials(
                clientId = "client-id",
                redirectUri = "https://example.com/callback",
            )

        val validResponse =
            AuthValidationResponse(
                clientId = "client-id",
                login = "appuser",
                userId = "app-user-id",
                scopes =
                    persistentSetOf(
                        "chat:read",
                        "chat:edit",
                        "user:read:follows",
                        "user:write:chat",
                    ),
            )
    }
}

private class FakeAuthApi(
    private val onValidate: () -> Result<AuthValidationResponse>,
) : AuthApi {
    override suspend fun validateToken(token: ApiToken): Result<AuthValidationResponse> = onValidate()

    override suspend fun revokeToken(
        clientId: String,
        token: ApiToken,
    ): Result<Unit> = Result.success(Unit)

    override fun getExternalAuthorizeUrl(
        oAuthAppCredentials: OAuthAppCredentials,
        scopes: Set<String>,
    ): Uri = error("Not used in tests")
}

private class FakePreferenceRepository(
    apiToken: ApiToken?,
) : PreferenceRepository {
    private val preferences = MutableStateFlow(AppPreferences(apiToken = apiToken))

    override val currentPreferences: Flow<AppPreferences> = preferences

    override suspend fun updatePreferences(update: (AppPreferences) -> AppPreferences) {
        preferences.update(update)
    }
}

private class TestDispatchersProvider(
    private val dispatcher: CoroutineDispatcher,
) : DispatchersProvider {
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
}
