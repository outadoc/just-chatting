package fr.outadoc.justchatting.ui.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.ui.chat.ChatActivity
import fr.outadoc.justchatting.ui.common.NavigationHandler
import fr.outadoc.justchatting.ui.login.LoginActivity
import fr.outadoc.justchatting.ui.onboarding.OnboardingScreen
import fr.outadoc.justchatting.util.parseChannelLogin
import fr.outadoc.justchatting.util.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewMainActivity : BaseActivity(), NavigationHandler {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Mdc3Theme {
                val state by viewModel.state.collectAsState()
                Crossfade(targetState = state) {
                    when (val currentState = state) {
                        is MainViewModel.State.Loading -> {}
                        is MainViewModel.State.LoggedOut -> {
                            OnboardingScreen(
                                onLoginClick = {
                                    if (currentState.causedByTokenExpiration) {
                                        toast(R.string.token_expired)
                                    }

                                    startActivity(Intent(this, LoginActivity::class.java))
                                }
                            )
                        }

                        is MainViewModel.State.LoggedIn -> {
                            HomeScreen(
                                onLogoutClick = {
                                    onLogout()
                                },
                                onChannelClick = { login ->
                                    viewChannel(login)
                                },
                                onOpenNotificationPreferences = {
                                    openSettingsIntent(action = Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                },
                                onOpenBubblePreferences = {
                                    openSettingsIntent(action = Settings.ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS)
                                }
                            )
                        }
                    }
                }
            }
        }

        if (savedInstanceState == null) {
            intent.parseChannelFromIntent()?.let { login ->
                viewChannel(login)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.parseChannelFromIntent()?.let { login ->
            viewChannel(login)
        }
    }

    private fun openSettingsIntent(action: String) {
        val intent = Intent().apply {
            this.action = action
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // for Android 5-7
            putExtra("app_package", packageName)
            putExtra("app_uid", applicationInfo.uid)

            // for Android 8 and above
            putExtra("android.provider.extra.APP_PACKAGE", packageName)
        }

        startActivity(intent)
    }

    private fun Intent.parseChannelFromIntent(): String? {
        if (action != Intent.ACTION_VIEW) return null
        return data?.parseChannelLogin()
    }

    override fun viewChannel(login: String) {
        startActivity(
            ChatActivity.createIntent(
                context = this,
                channelLogin = login
            )
        )
    }

    override fun openSearch() {
        viewModel.onOpenSearchRequested()
    }

    override fun openSettings() {
        viewModel.onOpenSettingsRequested()
    }

    private fun onLogout() {
        startActivity(
            Intent(this@NewMainActivity, LoginActivity::class.java)
        )
    }
}
