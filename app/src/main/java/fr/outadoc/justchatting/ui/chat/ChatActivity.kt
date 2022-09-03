package fr.outadoc.justchatting.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Display
import fr.outadoc.justchatting.ui.main.BaseActivity
import fr.outadoc.justchatting.util.formatChannelUri

class ChatActivity : BaseActivity() {

    companion object {
        private const val CHANNEL_LOGIN = "channel_login"

        fun createIntent(context: Context, channelLogin: String): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                data = formatChannelUri(channelLogin)
                action = Intent.ACTION_VIEW
                flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT

                putExtra(CHANNEL_LOGIN, channelLogin)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val channelLogin = intent.getStringExtra(CHANNEL_LOGIN)!!

        supportFragmentManager
            .beginTransaction()
            .replace(
                android.R.id.content,
                ChannelChatFragment.newInstance(login = channelLogin)
            )
            .commit()
    }

    private val isLaunchedFromBubbleCompat: Boolean
        get() = if (Build.VERSION.SDK_INT >= 31) {
            isLaunchedFromBubble
        } else {
            val displayId = if (Build.VERSION.SDK_INT >= 30) {
                display?.displayId
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.displayId
            }
            displayId != Display.DEFAULT_DISPLAY
        }
}
