package fr.outadoc.justchatting.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.ui.main.BaseActivity
import fr.outadoc.justchatting.util.createChannelDeeplink
import fr.outadoc.justchatting.util.createChannelExternalLink
import fr.outadoc.justchatting.util.isDark
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChatActivity : BaseActivity() {

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

        setContent {
            Mdc3Theme {
                ChannelChatScreen(
                    channelLogin = intent.getStringExtra(CHANNEL_LOGIN)!!
                )
            }
        }
    }

    @Composable
    fun ChannelChatScreen(channelLogin: String) {
        val viewModel: ChatViewModel by viewModel()
        val state by viewModel.state.collectAsState()

        val context = LocalContext.current
        val density = LocalDensity.current.density
        val uriHandler = LocalUriHandler.current

        val isDarkTheme = MaterialTheme.colorScheme.isDark

        val hostModeState = (state as? ChatViewModel.State.Chatting)?.hostModeState
        val user = (state as? ChatViewModel.State.Chatting)?.user

        val channelBranding: ChannelBranding = rememberChannelBranding(user)

        LaunchedEffect(hostModeState) {
            val targetUri = hostModeState?.targetChannelLogin?.createChannelDeeplink()
            if (targetUri != null && hostModeState.viewerCount != null) {
                // The broadcaster just launched a raid to another channel,
                // so let's go there as well!
                uriHandler.openUri(targetUri.toString())
            }
        }

        var isEmotePickerOpen by remember { mutableStateOf(false) }

        LaunchedEffect(channelLogin) {
            viewModel.loadChat(channelLogin)
        }

        BackHandler(isEmotePickerOpen) {
            isEmotePickerOpen = false
        }

        OnLifecycleEvent(
            onPause = {
                if (user != null && channelBranding.logo != null) {
                    ChatNotificationUtils.configureChatBubbles(
                        context = context,
                        channel = user,
                        channelLogo = channelBranding.logo
                    )
                }
            }
        )

        ChannelChatScreen(
            state = state,
            channelLogin = channelLogin,
            channelBranding = channelBranding,
            isEmotePickerOpen = isEmotePickerOpen,
            onWatchLiveClicked = {
                uriHandler.openUri(channelLogin.createChannelExternalLink().toString())
            },
            onMessageChange = viewModel::onMessageInputChanged,
            onToggleEmotePicker = {
                isEmotePickerOpen = !isEmotePickerOpen
            },
            onEmoteClick = { emote ->
                viewModel.appendEmote(emote, autocomplete = true)
            },
            onChatterClick = { chatter ->
                viewModel.appendChatter(chatter, autocomplete = true)
            },
            onClearReplyingTo = {
                viewModel.onReplyToMessage(null)
            },
            onOpenBubbleClicked = {
                if (user != null && channelBranding.logo != null) {
                    ChatNotificationUtils.configureChatBubbles(
                        context = context,
                        channel = user,
                        channelLogo = channelBranding.logo
                    )
                }
            },
            onSubmit = {
                viewModel.submit(
                    screenDensity = density,
                    isDarkTheme = isDarkTheme
                )
            },
            onReplyToMessage = viewModel::onReplyToMessage
        )
    }
}
