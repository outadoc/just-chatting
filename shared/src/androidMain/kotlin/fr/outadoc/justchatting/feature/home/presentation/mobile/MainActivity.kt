package fr.outadoc.justchatting.feature.home.presentation.mobile

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.lifecycle.lifecycleScope
import fr.outadoc.justchatting.feature.chat.presentation.mobile.ChatActivity
import fr.outadoc.justchatting.feature.home.presentation.MainRouterViewModel
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class MainActivity : AppCompatActivity() {

    private val viewModel: MainRouterViewModel by viewModel()

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        enableEdgeToEdge()

        DefaultEmojiCompatConfig.create(this)
            ?.setReplaceAll(true)
            ?.let { emojiConfig ->
                EmojiCompat.init(emojiConfig)
            }

        if (savedInstanceState == null) {
            intent.data?.toString()?.let { data ->
                viewModel.onReceiveIntent(data)
            }
        }

        lifecycleScope.launch {
            viewModel.events.collectLatest { event ->
                when (event) {
                    is MainRouterViewModel.Event.ViewChannel -> {
                        viewChannel(event.userId)
                    }

                    is MainRouterViewModel.Event.OpenInBrowser -> {
                        val intent = CustomTabsIntent.Builder().build()
                        intent.launchUrl(this@MainActivity, event.uri.toUri())
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

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    @Composable
    private fun App(sizeClass: WindowSizeClass) {
        val state by viewModel.state.collectAsState()
        Crossfade(
            targetState = state,
            label = "Login state animation",
        ) { currentState ->
            when (currentState) {
                is MainRouterViewModel.State.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is MainRouterViewModel.State.LoggedOut -> {
                    OnboardingScreen(
                        onLoginClick = {
                            viewModel.onLoginClick()
                        },
                    )
                }

                is MainRouterViewModel.State.LoggedIn -> {
                    MainRouter(
                        sizeClass = sizeClass,
                        onChannelClick = ::viewChannel,
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.toString()?.let { data ->
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

    private fun viewChannel(userId: String) {
        startActivity(
            ChatActivity.createIntent(
                context = this,
                userId = userId,
            ),
        )
    }
}
