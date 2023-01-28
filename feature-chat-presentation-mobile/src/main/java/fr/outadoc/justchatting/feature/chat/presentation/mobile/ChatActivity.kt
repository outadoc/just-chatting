package fr.outadoc.justchatting.feature.chat.presentation.mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import fr.outadoc.justchatting.utils.ui.AppTheme

class ChatActivity : AppCompatActivity() {

    companion object {
        private const val CHANNEL_LOGIN = "channel_login"

        fun createIntent(context: Context, channelLogin: String): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                data = channelLogin.createChannelDeeplink()
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT

                putExtra(CHANNEL_LOGIN, channelLogin)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        DefaultEmojiCompatConfig.create(this)
            ?.setReplaceAll(true)
            ?.let { emojiConfig ->
                EmojiCompat.init(emojiConfig)
            }

        setContent {
            AppTheme {
                ChannelChatScreen(
                    channelLogin = intent.getStringExtra(CHANNEL_LOGIN)!!,
                )
            }
        }
    }
}
