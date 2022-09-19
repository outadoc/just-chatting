package fr.outadoc.justchatting.ui.chat

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.WindowCompat
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.main.BaseActivity
import fr.outadoc.justchatting.ui.view.chat.StreamInfoDialog
import fr.outadoc.justchatting.util.createChannelDeeplink
import fr.outadoc.justchatting.util.createChannelExternalLink
import fr.outadoc.justchatting.util.isDark
import fr.outadoc.justchatting.util.isLaunchedFromBubbleCompat
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

    private val openInBubble: MutableState<(() -> Unit)?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val channelLogin = intent.getStringExtra(CHANNEL_LOGIN)!!

        setContent {
            Mdc3Theme {
                ChannelChatScreen(channelLogin = channelLogin)
            }
        }
    }

    @Composable
    fun ChannelChatScreen(channelLogin: String) {
        val viewModel: ChatViewModel by viewModel()
        val state by viewModel.state.collectAsState()

        val density = LocalDensity.current.density
        val isDarkTheme = MaterialTheme.colorScheme.isDark

        var isEmotePickerOpen by remember { mutableStateOf(false) }

        LaunchedEffect(channelLogin) {
            viewModel.loadChat(channelLogin)
        }

        BackHandler(isEmotePickerOpen) {
            isEmotePickerOpen = false
        }

        ChannelChatScreen(
            state = state,
            channelLogin = channelLogin,
            isEmotePickerOpen = isEmotePickerOpen,
            onChannelLogoLoaded = ::onChannelLogoLoaded,
            onWatchLiveClicked = ::onWatchLiveClicked,
            onOpenBubbleClicked = ::onOpenBubbleClicked,
            onStreamInfoClicked = ::onStreamInfoClicked,
            onColorContrastChanged = { isLight ->
                WindowCompat.getInsetsController(window, window.decorView)
                    .isAppearanceLightStatusBars = !isLight
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
            onReplyToMessage = viewModel::onReplyToMessage,
            onSubmit = {
                viewModel.submit(
                    screenDensity = density,
                    isDarkTheme = isDarkTheme
                )
            }
        )
    }

    private fun onChannelLogoLoaded(user: User, bitmap: Bitmap) {
        setTaskDescription(
            ActivityManager.TaskDescription(user.displayName, bitmap)
        )

        if (!isLaunchedFromBubbleCompat) {
            configureChatBubbles(user, bitmap)
        }
    }

    private fun onWatchLiveClicked(user: User) {
        startActivity(
            Intent(Intent.ACTION_VIEW, user.login.createChannelExternalLink())
        )
    }

    private fun onOpenBubbleClicked() {
        openInBubble.value?.invoke()
    }

    private fun onStreamInfoClicked(user: User) {
        StreamInfoDialog.newInstance(userId = user.id)
            .show(supportFragmentManager, "closeOnPip")
    }

    private fun configureChatBubbles(channel: User, channelLogo: Bitmap) {
        // Bubbles are only available on Android Q+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        ChatNotificationUtils.createGenericBubbleChannelIfNeeded(this)
            ?: return

        val icon = channelLogo.let { IconCompat.createWithBitmap(it) }

        val person: Person =
            Person.Builder()
                .setKey(channel.id)
                .setName(channel.displayName)
                .setIcon(icon)
                .build()

        ChatNotificationUtils.createShortcutForChannel(
            context = this,
            intent = createIntent(
                context = this,
                channelLogin = channel.login
            ),
            channelId = channel.id,
            channelName = channel.displayName,
            person = person,
            icon = icon
        )

        openInBubble.value = {
            ChatNotificationUtils.createBubble(
                context = this,
                user = channel,
                icon = icon,
                person = person
            )
        }
    }

    override fun onPause() {
        super.onPause()
        openInBubble.value?.invoke()
    }

    override fun onDestroy() {
        super.onDestroy()
        openInBubble.value = null
    }
}
