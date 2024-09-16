package fr.outadoc.justchatting.feature.chat.presentation.mobile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.emoji2.text.DefaultEmojiCompatConfig
import androidx.emoji2.text.EmojiCompat
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import fr.outadoc.justchatting.utils.presentation.AppTheme

public class ChatActivity : AppCompatActivity() {

    internal companion object {
        private const val CHANNEL_USER_ID = "channel_user_id"

        fun createIntent(context: Context, userId: String): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                data = createChannelDeeplink(userId).toUri()
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT

                putExtra(CHANNEL_USER_ID, userId)
            }
        }

        fun createGlanceAction(userId: String): Action {
            // TODO use compose navigation instead
            return actionStartActivity<ChatActivity>(
                actionParametersOf(
                    ActionParameters.Key<String>(CHANNEL_USER_ID) to userId,
                ),
            )
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
                    userId = intent.getStringExtra(CHANNEL_USER_ID)!!,
                    showBackButton = false,
                )
            }
        }
    }
}
