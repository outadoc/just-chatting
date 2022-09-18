package fr.outadoc.justchatting.ui.chat

import android.app.ActivityManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.databinding.FragmentChannelBinding
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.view.chat.StreamInfoDialog
import fr.outadoc.justchatting.util.formatChannelUri
import fr.outadoc.justchatting.util.isDark
import fr.outadoc.justchatting.util.isLaunchedFromBubbleCompat
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

object EmojiPickerDefaults {
    val DefaultHeight = 350.dp
}

class ChannelChatFragment : Fragment() {

    companion object {
        private const val CHANNEL_LOGIN = "channel_login"

        fun newInstance(login: String) =
            ChannelChatFragment().apply {
                arguments = bundleOf(CHANNEL_LOGIN to login)
            }
    }

    private val channelViewModel: ChannelChatViewModel by viewModel()
    private val chatViewModel: ChatViewModel by sharedViewModel()

    private var viewHolder: FragmentChannelBinding? = null

    private var openInBubble: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        channelViewModel.loadStream(channelLogin = requireArguments().getString(CHANNEL_LOGIN)!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val argLogin = requireArguments().getString(CHANNEL_LOGIN)!!

        viewHolder = FragmentChannelBinding.inflate(inflater, container, false).apply {
            composeViewChat.apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    Mdc3Theme {
                        val chatState by chatViewModel.state.observeAsState(ChatViewModel.State.Initial)
                        val channelState by channelViewModel.state.observeAsState(
                            ChannelChatViewModel.State.Loading
                        )

                        val density = LocalDensity.current.density
                        val isDarkTheme = MaterialTheme.colorScheme.isDark

                        var isEmotePickerOpen by remember { mutableStateOf(false) }

                        BackHandler(isEmotePickerOpen) {
                            isEmotePickerOpen = false
                        }

                        ChannelChatScreen(
                            chatState = chatState,
                            channelState = channelState,
                            channelLogin = argLogin,
                            isEmotePickerOpen = isEmotePickerOpen,
                            onChannelLogoLoaded = ::onChannelLogoLoaded,
                            onWatchLiveClicked = ::onWatchLiveClicked,
                            onOpenBubbleClicked = ::onOpenBubbleClicked,
                            onStreamInfoClicked = ::onStreamInfoClicked,
                            onColorContrastChanged = { isLight ->
                                activity?.let { activity ->
                                    WindowCompat.getInsetsController(
                                        activity.window,
                                        activity.window.decorView
                                    ).isAppearanceLightStatusBars = !isLight
                                }
                            },
                            onMessageChange = chatViewModel::onMessageInputChanged,
                            onToggleEmotePicker = {
                                isEmotePickerOpen = !isEmotePickerOpen
                            },
                            onEmoteClick = { emote ->
                                chatViewModel.appendEmote(emote, autocomplete = true)
                            },
                            onChatterClick = { chatter ->
                                chatViewModel.appendChatter(chatter, autocomplete = true)
                            },
                            onClearReplyingTo = {
                                chatViewModel.onReplyToMessage(null)
                            },
                            onReplyToMessage = chatViewModel::onReplyToMessage,
                            onSubmit = {
                                chatViewModel.submit(
                                    screenDensity = density,
                                    isDarkTheme = isDarkTheme
                                )
                            }
                        )
                    }
                }
            }
        }

        return viewHolder?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        channelViewModel.state.observe(viewLifecycleOwner) { state ->
            state.loadedUser?.let { user ->
                chatViewModel.startLive(
                    channelId = user.id,
                    channelLogin = user.login,
                    channelName = user.display_name
                )
            }
        }
    }

    private fun onChannelLogoLoaded(user: User, bitmap: Bitmap) {
        activity?.apply {
            setTaskDescription(
                ActivityManager.TaskDescription(user.display_name, bitmap)
            )

            if (!isLaunchedFromBubbleCompat) {
                configureChatBubbles(user, bitmap)
            }
        }
    }

    private fun onWatchLiveClicked(user: User) {
        startActivity(
            Intent(Intent.ACTION_VIEW, formatChannelUri(user.login))
        )
    }

    private fun onOpenBubbleClicked() {
        openInBubble?.invoke()
    }

    private fun onStreamInfoClicked(user: User) {
        StreamInfoDialog.newInstance(userId = user.id)
            .show(childFragmentManager, "closeOnPip")
    }

    private fun configureChatBubbles(channel: User, channelLogo: Bitmap) {
        val context = context ?: return

        // Bubbles are only available on Android Q+
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return

        ChatNotificationUtils.createGenericBubbleChannelIfNeeded(context)
            ?: return

        val icon = channelLogo.let { IconCompat.createWithBitmap(it) }

        val person: Person =
            Person.Builder()
                .setKey(channel.id)
                .setName(channel.display_name)
                .setIcon(icon)
                .build()

        ChatNotificationUtils.createShortcutForChannel(
            context = context,
            intent = ChatActivity.createIntent(
                context = context,
                channelLogin = channel.login
            ),
            channelId = channel.id,
            channelName = channel.display_name,
            person = person,
            icon = icon
        )

        openInBubble = {
            ChatNotificationUtils.createBubble(
                context = context,
                user = channel,
                icon = icon,
                person = person
            )
        }
    }

    override fun onPause() {
        super.onPause()
        openInBubble?.invoke()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewHolder = null
        openInBubble = null
    }
}
