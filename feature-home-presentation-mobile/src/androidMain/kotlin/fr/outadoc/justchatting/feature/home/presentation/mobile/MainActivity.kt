package fr.outadoc.justchatting.feature.home.presentation.mobile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.Crossfade
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.lifecycle.lifecycleScope
import fr.outadoc.justchatting.feature.chat.presentation.mobile.ChatActivity
import fr.outadoc.justchatting.feature.home.presentation.MainRouterViewModel
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.toast
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainRouterViewModel by viewModel()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        DefaultEmojiCompatConfig.create(this)
            ?.setReplaceAll(true)
            ?.let { emojiConfig ->
                EmojiCompat.init(emojiConfig)
            }

        if (savedInstanceState == null) {
            intent.data?.let { data ->
                viewModel.onReceiveIntent(data)
            }
        }

        lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is MainRouterViewModel.Event.ViewChannel -> {
                        viewChannel(event.login)
                    }

                    is MainRouterViewModel.Event.OpenInBrowser -> {
                        val intent = CustomTabsIntent.Builder().build()
                        intent.launchUrl(this@MainActivity, event.uri)
                    }
                }
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
        Crossfade(
            targetState = state,
            label = "Login state animation",
        ) { currentState ->
            when (currentState) {
                is MainRouterViewModel.State.Loading -> {}
                is MainRouterViewModel.State.LoggedOut -> {
                    LaunchedEffect(currentState) {
                        if (currentState.causedByTokenExpiration) {
                            toast(MR.strings.token_expired.resourceId)
                        }
                    }

                    OnboardingScreen(
                        onLoginClick = {
                            viewModel.onLoginClick()
                        },
                    )
                }

                is MainRouterViewModel.State.LoggedIn -> {
                    MainRouter(
                        sizeClass = sizeClass,
                        onChannelClick = { login ->
                            viewChannel(login)
                        },
                        onOpenNotificationPreferences = {
                            openSettingsIntent(action = "android.settings.APP_NOTIFICATION_SETTINGS")
                        },
                        onOpenBubblePreferences = {
                            if (Build.VERSION.SDK_INT >= 29) {
                                openSettingsIntent(action = Settings.ACTION_APP_NOTIFICATION_BUBBLE_SETTINGS)
                            }
                        },
                        onOpenAccessibilityPreferences = {
                            openSettingsIntent(action = "android.settings.ACCESSIBILITY_SETTINGS")
                        },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.data?.let { data ->
            viewModel.onReceiveIntent(data)
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

    private fun viewChannel(login: String) {
        startActivity(
            ChatActivity.createIntent(
                context = this,
                channelLogin = login,
            ),
        )
    }
}
