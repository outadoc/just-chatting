package fr.outadoc.justchatting.ui.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.ui.chat.ChatActivity
import fr.outadoc.justchatting.ui.login.LoginActivity
import fr.outadoc.justchatting.ui.onboarding.OnboardingScreen
import fr.outadoc.justchatting.ui.theme.AppTheme
import fr.outadoc.justchatting.util.parseChannelLogin
import fr.outadoc.justchatting.util.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    private val viewModel: MainViewModel by viewModel()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            intent.parseChannelFromIntent()?.let { login ->
                viewChannel(login)
            }
        }

        setContent {
            AppTheme {
                App(sizeClass = calculateWindowSizeClass(this))
            }
        }
    }

    @Composable
    private fun App(sizeClass: WindowSizeClass) {
        val state by viewModel.state.collectAsState()
        Crossfade(targetState = state) { currentState ->
            when (currentState) {
                is MainViewModel.State.Loading -> {}
                is MainViewModel.State.LoggedOut -> {
                    LaunchedEffect(currentState) {
                        if (currentState.causedByTokenExpiration) {
                            toast(R.string.token_expired)
                        }
                    }

                    OnboardingScreen(
                        onLoginClick = {
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                    )
                }

                is MainViewModel.State.LoggedIn -> {
                    HomeScreen(
                        sizeClass = sizeClass,
                        onChannelClick = { login ->
                            viewChannel(login)
                        },
                        onOpenNotificationPreferences = {
                            if (Build.VERSION.SDK_INT >= 26) {
                                openSettingsIntent(action = Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            }
                        },
                        onOpenBubblePreferences = {
                            if (Build.VERSION.SDK_INT >= 29) {
                                openSettingsIntent(action = Settings.ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS)
                            }
                        }
                    )
                }
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

    private fun viewChannel(login: String) {
        startActivity(
            ChatActivity.createIntent(
                context = this,
                channelLogin = login
            )
        )
    }
}
