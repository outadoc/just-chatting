package fr.outadoc.justchatting.ui.chat

import android.app.ActivityManager
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.databinding.FragmentChannelBinding
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.view.chat.StreamInfoDialog
import fr.outadoc.justchatting.util.formatChannelUri
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
                        ChannelChatScreen(
                            chatViewModel = chatViewModel,
                            channelChatViewModel = channelViewModel,
                            channelLogin = argLogin,
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
