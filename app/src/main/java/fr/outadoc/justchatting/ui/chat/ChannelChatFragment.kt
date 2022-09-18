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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.FragmentChannelBinding
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.common.ensureMinimumAlpha
import fr.outadoc.justchatting.ui.common.isLightColor
import fr.outadoc.justchatting.ui.view.chat.StreamInfoDialog
import fr.outadoc.justchatting.ui.view.emotes.EmotePicker
import fr.outadoc.justchatting.util.formatChannelUri
import fr.outadoc.justchatting.util.generateAsync
import fr.outadoc.justchatting.util.isDark
import fr.outadoc.justchatting.util.isLaunchedFromBubbleCompat
import fr.outadoc.justchatting.util.loadImageToBitmap
import fr.outadoc.justchatting.util.shortToast
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

    @OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
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
                        val keyboardController = LocalSoftwareKeyboardController.current
                        val clipboard = LocalClipboardManager.current
                        val haptic = LocalHapticFeedback.current
                        val context = LocalContext.current

                        val state by chatViewModel.state.observeAsState(ChatViewModel.State.Initial)
                        val channelState by channelViewModel.state.observeAsState(
                            ChannelChatViewModel.State.Loading
                        )

                        var isEmotePickerOpen by remember { mutableStateOf(false) }

                        val stream = (channelState as? ChannelChatViewModel.State.Loaded)?.stream
                        val user = (channelState as? ChannelChatViewModel.State.Loaded)?.loadedUser

                        var logo: Bitmap? by remember { mutableStateOf(null) }
                        var swatch: Swatch? by remember { mutableStateOf(null) }

                        LaunchedEffect(user) {
                            user?.profile_image_url ?: return@LaunchedEffect

                            logo = loadImageToBitmap(
                                context = context,
                                imageUrl = user.profile_image_url,
                                circle = true,
                                width = 256,
                                height = 256
                            )

                            swatch = logo?.let {
                                val palette = Palette.Builder(it).generateAsync()
                                (palette?.dominantSwatch ?: palette?.dominantSwatch)
                            }

                            logo?.let { logo ->
                                onChannelLogoLoaded(user, logo)
                            }
                        }

                        BackHandler(isEmotePickerOpen) {
                            isEmotePickerOpen = false
                        }

                        LaunchedEffect(isEmotePickerOpen) {
                            if (isEmotePickerOpen) {
                                keyboardController?.hide()
                            }
                        }

                        Column(verticalArrangement = Arrangement.SpaceEvenly) {
                            TopAppBar(
                                colors = swatch.toolbarColors(),
                                title = {
                                    Column {
                                        Text(
                                            text = user?.display_name ?: argLogin,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (stream?.title != null) {
                                            Text(
                                                text = stream.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                },
                                navigationIcon = {
                                    logo?.let { logo ->
                                        Image(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .padding(horizontal = 8.dp),
                                            bitmap = logo.asImageBitmap(),
                                            contentDescription = null
                                        )
                                    }
                                },
                                actions = {
                                    user?.let { user ->
                                        IconButton(onClick = { onWatchLiveClicked(user) }) {
                                            Icon(
                                                Icons.Default.LiveTv,
                                                contentDescription = stringResource(R.string.watch_live)
                                            )
                                        }

                                        IconButton(onClick = { onOpenBubbleClicked() }) {
                                            Icon(
                                                Icons.Default.OpenInNew,
                                                contentDescription = stringResource(R.string.menu_item_openInBubble)
                                            )
                                        }

                                        IconButton(onClick = { onStreamInfoClicked(user) }) {
                                            Icon(
                                                Icons.Default.Info,
                                                contentDescription = stringResource(R.string.stream_info)
                                            )
                                        }
                                    }
                                }
                            )

                            ChatScreen(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                state = state,
                                onMessageLongClick = { item ->
                                    item.data?.message?.let { rawMessage ->
                                        clipboard.setText(AnnotatedString(rawMessage))
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        context.shortToast(R.string.chat_copiedToClipboard)
                                    }
                                },
                                onReplyToMessage = chatViewModel::onReplyToMessage
                            )

                            ChatSlowModeProgress(
                                modifier = Modifier.fillMaxWidth(),
                                state = state
                            )

                            val density = LocalDensity.current.density
                            val isDarkTheme = MaterialTheme.colorScheme.isDark

                            ChatInput(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .then(
                                        if (!isEmotePickerOpen) Modifier.navigationBarsPadding()
                                        else Modifier
                                    )
                                    .fillMaxWidth(),
                                state = state,
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
                                onSubmit = {
                                    chatViewModel.submit(
                                        screenDensity = density,
                                        isDarkTheme = isDarkTheme
                                    )
                                }
                            )

                            var imeHeight by remember { mutableStateOf(EmojiPickerDefaults.DefaultHeight) }

                            val currentImeHeight = WindowInsets.ime
                                .asPaddingValues()
                                .calculateBottomPadding()

                            LaunchedEffect(currentImeHeight) {
                                if (currentImeHeight > imeHeight) {
                                    imeHeight = currentImeHeight
                                }
                            }

                            AnimatedVisibility(visible = isEmotePickerOpen) {
                                EmotePicker(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(imeHeight),
                                    onEmoteClick = ::appendEmote,
                                    state = state
                                )
                            }
                        }
                    }
                }
            }
        }
        return viewHolder?.root
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Swatch?.toolbarColors(): TopAppBarColors {
        if (this == null) return TopAppBarDefaults.smallTopAppBarColors()

        val backgroundColor = rgb
        val textColor = ensureMinimumAlpha(
            foreground = titleTextColor,
            background = backgroundColor
        )

        LaunchedEffect(textColor) {
            activity?.let { activity ->
                WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                    .isAppearanceLightStatusBars = !textColor.isLightColor
            }
        }

        return TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Color(backgroundColor),
            titleContentColor = Color(textColor),
            actionIconContentColor = Color(textColor)
        )
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

    private fun appendEmote(emote: Emote) {
        chatViewModel.appendEmote(emote, autocomplete = false)
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
