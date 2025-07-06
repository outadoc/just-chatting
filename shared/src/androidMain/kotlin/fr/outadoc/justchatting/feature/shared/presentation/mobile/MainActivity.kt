package fr.outadoc.justchatting.feature.shared.presentation.mobile

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import com.eygraber.uri.Uri
import com.eygraber.uri.toAndroidUri
import com.eygraber.uri.toKmpUri
import fr.outadoc.justchatting.feature.chat.presentation.mobile.createChannelDeeplink
import fr.outadoc.justchatting.feature.shared.presentation.DeeplinkReceiver
import org.koin.android.ext.android.inject

internal class MainActivity : AppCompatActivity() {

    companion object {
        private const val CHANNEL_USER_ID = "channel_user_id"

        fun createIntent(context: Context, userId: String): Intent {
            return Intent(context, MainActivity::class.java).apply {
                data = createChannelDeeplink(userId).toAndroidUri()
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT

                putExtra(CHANNEL_USER_ID, userId)
            }
        }

        fun createGlanceAction(userId: String): Action {
            return actionStartActivity<MainActivity>(
                parameters = actionParametersOf(
                    ActionParameters.Key<String>(CHANNEL_USER_ID) to userId,
                ),
            )
        }
    }

    private val deeplinkReceiver: DeeplinkReceiver by inject()

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
            handleIntent(intent)
        }

        setContent {
            App(
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
                onShareLogs = ::shareLogs,
                onShowAuthPage = { uri ->
                    val intent = CustomTabsIntent.Builder().build()
                    intent.launchUrl(this@MainActivity, uri.toAndroidUri())
                },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val userIdFromParams: String? =
            intent.getStringExtra(CHANNEL_USER_ID)

        val uri: Uri? =
            if (userIdFromParams != null) {
                createChannelDeeplink(userId = userIdFromParams)
            } else {
                intent.data?.toKmpUri()
            }

        if (uri != null) {
            deeplinkReceiver.onDeeplinkReceived(uri)
        }
    }

    private fun shareLogs(uri: Uri) {
        val sendIntent: Intent =
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri.toAndroidUri())
                type = "application/gzip"
            }

        startActivity(
            Intent.createChooser(sendIntent, null),
        )
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
}
