package fr.outadoc.justchatting.ui.chat

import android.os.Build
import android.os.Bundle
import fr.outadoc.justchatting.ui.main.BaseActivity
import fr.outadoc.justchatting.util.C

class ChatActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager
            .beginTransaction()
            .replace(
                android.R.id.content,
                ChannelChatFragment.newInstance(
                    id = intent.getStringExtra(C.CHANNEL_ID),
                    login = intent.getStringExtra(C.CHANNEL_LOGIN),
                    name = intent.getStringExtra(C.CHANNEL_DISPLAYNAME),
                    channelLogo = intent.getStringExtra(C.CHANNEL_PROFILEIMAGE),
                    showBackButton = !isLaunchedFromBubbleCompat
                )
            )
            .commit()
    }

    override fun onBackPressed() {
        when {
            isLaunchedFromBubbleCompat -> super.onBackPressed()
            else -> finish()
        }
    }

    private val isLaunchedFromBubbleCompat: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            isLaunchedFromBubble
        } else {
            false
        }
}
