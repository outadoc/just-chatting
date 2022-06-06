package com.github.andreyasadchy.xtra.ui.chat

import android.os.Build
import android.os.Bundle
import com.github.andreyasadchy.xtra.ui.main.BaseActivity
import com.github.andreyasadchy.xtra.util.C

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

    private val isLaunchedFromBubbleCompat: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            isLaunchedFromBubble
        } else {
            false
        }
}