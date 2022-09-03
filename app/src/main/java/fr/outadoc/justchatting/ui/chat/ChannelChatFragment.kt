package fr.outadoc.justchatting.ui.chat

import android.app.Activity
import android.app.ActivityManager
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.InsetDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import androidx.palette.graphics.Palette.Swatch
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.shape.MaterialShapeDrawable
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.FragmentChannelBinding
import fr.outadoc.justchatting.model.chat.Emote
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.common.Scrollable
import fr.outadoc.justchatting.ui.common.ensureMinimumAlpha
import fr.outadoc.justchatting.ui.common.isLightColor
import fr.outadoc.justchatting.ui.view.chat.EmotesFragment
import fr.outadoc.justchatting.ui.view.chat.MessageClickedDialog
import fr.outadoc.justchatting.ui.view.chat.OnEmoteClickedListener
import fr.outadoc.justchatting.ui.view.chat.StreamInfoDialog
import fr.outadoc.justchatting.util.formatChannelUri
import fr.outadoc.justchatting.util.generateAsync
import fr.outadoc.justchatting.util.hideKeyboard
import fr.outadoc.justchatting.util.isDarkMode
import fr.outadoc.justchatting.util.loadImageToBitmap
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChannelChatFragment :
    Fragment(),
    MessageClickedDialog.OnButtonClickListener,
    OnEmoteClickedListener,
    Scrollable {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        channelViewModel.loadStream(
            channelLogin = requireArguments().getString(CHANNEL_LOGIN)!!
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewHolder = FragmentChannelBinding.inflate(inflater, container, false)
        return viewHolder?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.onBackPressedDispatcher?.addCallback(this) {
            viewHolder?.apply {
                if (!chatInputView.hideEmotesMenu()) {
                    isEnabled = false
                    activity?.onBackPressed()
                    isEnabled = true
                }
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            viewHolder?.apply {
                toolbar.setPadding(
                    toolbar.paddingLeft,
                    insets.top,
                    toolbar.paddingRight,
                    toolbar.paddingBottom
                )
            }
            windowInsets
        }

        channelViewModel.state.observe(viewLifecycleOwner) { state ->
            viewHolder?.apply {
                updateStreamLayout(state.stream)

                state.loadedUser?.let { user ->
                    updateUserLayout(user)

                    chatViewModel.startLive(
                        channelId = user.id,
                        channelLogin = user.login,
                        channelName = user.display_name
                    )
                }

                state.user.login?.let { login ->
                    chatView.setUsername(login)
                }

                chatView.showTimestamps = state.showTimestamps
                chatView.animateEmotes = state.animateEmotes
                chatInputView.animateEmotes = state.animateEmotes
            }
        }

        viewHolder?.apply {

            chatView.setOnMessageClickListener { original, formatted, userId ->
                hideKeyboard()

                MessageClickedDialog.newInstance(
                    originalMessage = original,
                    formattedMessage = formatted,
                    userId = userId
                ).show(childFragmentManager, "closeOnPip")
            }

            chatInputView.setOnMessageSendListener { message ->
                chatViewModel.send(
                    message = message,
                    screenDensity = requireContext().resources.displayMetrics.density,
                    isDarkTheme = requireContext().isDarkMode
                )

                chatView.scrollToBottom()
            }

            chatInputView.emotePickerSelectedTab.observe(viewLifecycleOwner) { position ->
                val fragment = childFragmentManager.findFragmentByTag("f$position")
                (fragment as? Scrollable)?.scrollToTop()
            }

            chatInputView.emotePickerAdapter =
                object : FragmentStateAdapter(this@ChannelChatFragment) {

                    override fun createFragment(position: Int): Fragment =
                        EmotesFragment.newInstance(position)

                    override fun getItemCount(): Int = 3
                }
        }

        chatViewModel.state.observe(viewLifecycleOwner) { state ->
            viewHolder?.apply {
                with(chatInputView) {
                    setMessagePostConstraint(state.messagePostConstraint)
                    setAutocompleteItems(
                        emotes = state.allEmotes,
                        chatters = state.chatters
                    )
                }

                with(chatView) {
                    setEmotes(state.allEmotes)
                    submitList(state.chatMessages)
                    notifyRoomState(state.roomState)
                    addGlobalBadges(state.globalBadges)
                    addChannelBadges(state.channelBadges)
                    addCheerEmotes(state.cheerEmotes)
                }
            }
        }
    }

    private fun FragmentChannelBinding.updateStreamLayout(stream: Stream?) {
        if (stream?.title != null) {
            toolbar.subtitle = stream.title.trim()
        } else {
            toolbar.subtitle = null
        }
    }

    private fun FragmentChannelBinding.loadUserAvatar(user: User) {
        val context = context ?: return
        val logo = user.channelLogo ?: return

        val size = context.resources.getDimension(R.dimen.chat_streamPictureSize).toInt()
        val endMargin = context.resources.getDimension(R.dimen.chat_streamPictureMarginEnd).toInt()

        lifecycleScope.launch {
            val drawable = loadImageToBitmap(
                context = context,
                imageUrl = logo,
                circle = true,
                width = size,
                height = size
            )

            toolbar.logo = InsetDrawable(drawable, 0, 0, endMargin, 0)

            drawable?.bitmap?.let { bitmap ->
                val palette = Palette.Builder(bitmap).generateAsync()
                (palette?.dominantSwatch ?: palette?.dominantSwatch)
                    ?.let { swatch ->
                        updateToolbarColor(swatch)
                    }

                activity?.setTaskDescription(
                    ActivityManager.TaskDescription(user.display_name, bitmap)
                )
            }
        }
    }

    private fun FragmentChannelBinding.updateToolbarColor(swatch: Swatch) {
        val backgroundColor = swatch.rgb
        val textColor = ensureMinimumAlpha(
            foreground = swatch.titleTextColor,
            background = backgroundColor
        )

        ViewCompat.setBackground(
            toolbar,
            MaterialShapeDrawable.createWithElevationOverlay(
                toolbar.context,
                ViewCompat.getElevation(toolbar)
            ).apply {
                fillColor = ColorStateList.valueOf(backgroundColor)
            }
        )

        toolbar.setNavigationIconTint(textColor)
        toolbar.setTitleTextColor(textColor)
        toolbar.setSubtitleTextColor(textColor)
        toolbar.menu.forEach { item ->
            DrawableCompat.setTint(item.icon, textColor)
        }

        activity?.let { activity ->
            WindowCompat.getInsetsController(activity.window, activity.window.decorView)
                .isAppearanceLightStatusBars = !textColor.isLightColor
        }
    }

    private fun FragmentChannelBinding.updateUserLayout(user: User) {
        toolbar.apply {
            title = user.display_name

            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.watchLive -> {
                        startActivity(
                            Intent(Intent.ACTION_VIEW, formatChannelUri(user.login))
                        )
                        true
                    }
                    R.id.info -> {
                        StreamInfoDialog.newInstance(userId = user.id)
                            .show(childFragmentManager, "closeOnPip")
                        true
                    }
                    else -> false
                }
            }
        }

        loadUserAvatar(user)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            viewHolder?.appBar?.setExpanded(false, false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 3 && resultCode == Activity.RESULT_OK) {
            requireActivity().recreate()
        }
    }

    private fun hideKeyboard() {
        viewHolder?.apply {
            chatInputView.hideKeyboard()
            chatInputView.clearFocus()
        }
    }

    override fun onEmoteClicked(emote: Emote) {
        viewHolder?.chatInputView?.appendEmote(emote)
    }

    override fun onReplyClicked(userName: String) {
        viewHolder?.chatInputView?.reply(userName)
    }

    override fun onCopyMessageClicked(message: String) {
        viewHolder?.chatInputView?.setMessage(message)
    }

    override fun onViewProfileClicked(login: String) {
        val context = context ?: return
        context.startActivity(
            ChatActivity.createIntent(
                context = context,
                channelLogin = login
            )
        )
    }

    override fun scrollToTop() {
        viewHolder?.appBar?.setExpanded(true, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewHolder = null
    }
}
