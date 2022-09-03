package fr.outadoc.justchatting.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.net.toUri
import fr.outadoc.justchatting.ui.main.BaseActivity
import fr.outadoc.justchatting.util.C

class ChatActivity : BaseActivity() {

    companion object {
        fun createIntent(
            context: Context,
            channelId: String,
            channelLogin: String,
            channelName: String,
            channelLogo: String
        ): Intent {
            return Intent(context, ChatActivity::class.java).apply {
                data = "https://twitch.tv/$channelLogin".toUri()
                action = Intent.ACTION_VIEW
                flags = 0

                putExtra(C.CHANNEL_ID, channelId)
                putExtra(C.CHANNEL_LOGIN, channelLogin)
                putExtra(C.CHANNEL_DISPLAYNAME, channelName)
                putExtra(C.CHANNEL_PROFILEIMAGE, channelLogo)
            }
        }
    }

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
                    channelLogo = intent.getStringExtra(C.CHANNEL_PROFILEIMAGE)
                )
            )
            .commit()
    }
}
