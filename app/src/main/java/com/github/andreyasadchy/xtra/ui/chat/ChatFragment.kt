package com.github.andreyasadchy.xtra.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.model.LoggedIn
import com.github.andreyasadchy.xtra.model.User
import com.github.andreyasadchy.xtra.model.chat.Emote
import com.github.andreyasadchy.xtra.ui.common.BaseNetworkFragment
import com.github.andreyasadchy.xtra.ui.main.MainActivity
import com.github.andreyasadchy.xtra.ui.player.BasePlayerFragment
import com.github.andreyasadchy.xtra.ui.view.chat.ChatView
import com.github.andreyasadchy.xtra.ui.view.chat.MessageClickedDialog
import com.github.andreyasadchy.xtra.util.*
import kotlinx.android.synthetic.main.view_chat.view.*

class ChatFragment : BaseNetworkFragment(), LifecycleListener, MessageClickedDialog.OnButtonClickListener {

    private val viewModel by viewModels<ChatViewModel> { viewModelFactory }
    private lateinit var chatView: ChatView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false).also { chatView = it as ChatView }
    }

    override fun initialize() {
        val args = requireArguments()
        val channelId = args.getString(KEY_CHANNEL_ID)
        val channelLogin = args.getString(KEY_CHANNEL_LOGIN)
        val channelName = args.getString(KEY_CHANNEL_NAME)
        val user = User.get(requireContext())
        val userIsLoggedIn = user is LoggedIn
        val useSSl = requireContext().prefs().getBoolean(C.CHAT_USE_SSL, true)
        val helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, "")
        val gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, "") ?: ""
        val showUserNotice = requireContext().prefs().getBoolean(C.CHAT_SHOW_USERNOTICE, true)
        val showClearMsg = requireContext().prefs().getBoolean(C.CHAT_SHOW_CLEARMSG, true)
        val showClearChat = requireContext().prefs().getBoolean(C.CHAT_SHOW_CLEARCHAT, true)
        val enableRecentMsg = requireContext().prefs().getBoolean(C.CHAT_RECENT, true)
        val recentMsgLimit = requireContext().prefs().getInt(C.CHAT_RECENT_LIMIT, 100)
        val disableChat = requireContext().prefs().getBoolean(C.CHAT_DISABLE, false)
        val isLive = args.getBoolean(KEY_IS_LIVE)
        val enableChat = if (disableChat) {
            false
        } else {
            if (isLive) {
                viewModel.startLive(useSSl, user, helixClientId, gqlClientId, channelId, channelLogin, channelName, showUserNotice, showClearMsg, showClearChat, enableRecentMsg, recentMsgLimit.toString())
                chatView.init(this)
                chatView.setCallback(viewModel)
                if (userIsLoggedIn) {
                    user.login?.let { chatView.setUsername(it) }
                    chatView.setChatters(viewModel.chatters)
                    val emotesObserver = Observer(chatView::addEmotes)
                    viewModel.emotesFromSets.observe(viewLifecycleOwner, emotesObserver)
                    viewModel.recentEmotes.observe(viewLifecycleOwner, emotesObserver)
                    viewModel.newChatter.observe(viewLifecycleOwner, Observer(chatView::addChatter))
                }
                true
            } else {
                args.getString(KEY_VIDEO_ID).let {
                    if (it != null && !args.getBoolean(KEY_START_TIME_EMPTY)) {
                        chatView.init(this)
                        val getCurrentPosition = (parentFragment as ChatReplayPlayerFragment)::getCurrentPosition
                        viewModel.startReplay(user, helixClientId, gqlClientId, channelId, it, args.getDouble(KEY_START_TIME), getCurrentPosition)
                        true
                    } else {
                        chatView.chatReplayUnavailable.visible()
                        false
                    }
                }
            }
        }
        if (enableChat) {
            chatView.enableChatInteraction(isLive && userIsLoggedIn)
            viewModel.chatMessages.observe(viewLifecycleOwner, Observer(chatView::submitList))
            viewModel.newMessage.observe(viewLifecycleOwner) { chatView.notifyMessageAdded() }
            viewModel.recentMessages.observe(viewLifecycleOwner) { chatView.addRecentMessages(it) }
            viewModel.globalBadges.observe(viewLifecycleOwner, Observer(chatView::addGlobalBadges))
            viewModel.channelBadges.observe(viewLifecycleOwner, Observer(chatView::addChannelBadges))
            viewModel.otherEmotes.observe(viewLifecycleOwner, Observer(chatView::addEmotes))
            viewModel.cheerEmotes.observe(viewLifecycleOwner, Observer(chatView::addCheerEmotes))
            viewModel.emotesLoaded.observe(viewLifecycleOwner) { chatView.notifyEmotesLoaded() }
            viewModel.roomState.observe(viewLifecycleOwner) { chatView.notifyRoomState(it) }
            viewModel.command.observe(viewLifecycleOwner) { chatView.notifyCommand(it) }
        }
    }

    fun isActive(): Boolean? {
        return (viewModel.chat as? ChatViewModel.LiveChatController)?.isActive()
    }

    fun disconnect() {
        (viewModel.chat as? ChatViewModel.LiveChatController)?.disconnect()
    }

    fun hideKeyboard() {
        chatView.hideKeyboard()
        chatView.clearFocus()
    }

    fun hideEmotesMenu() = chatView.hideEmotesMenu()

    fun appendEmote(emote: Emote) {
        chatView.appendEmote(emote)
    }

    override fun onReplyClicked(userName: String) {
        chatView.reply(userName)
    }

    override fun onCopyMessageClicked(message: String) {
        chatView.setMessage(message)
    }

    override fun onViewProfileClicked(id: String?, login: String?, name: String?, channelLogo: String?) {
        (requireActivity() as MainActivity).viewChannel(id, login, name, channelLogo)
        (parentFragment as? BasePlayerFragment)?.minimize()
    }

    override fun onNetworkRestored() {
        if (isResumed) {
            viewModel.start()
        }
    }

    override fun onMovedToBackground() {
        if (!requireArguments().getBoolean(KEY_IS_LIVE) || !requireContext().prefs().getBoolean(C.PLAYER_KEEP_CHAT_OPEN, false) || requireContext().prefs().getBoolean(C.CHAT_DISABLE, false)) {
            viewModel.stop()
        }
    }

    override fun onMovedToForeground() {
        if (!requireArguments().getBoolean(KEY_IS_LIVE) || !requireContext().prefs().getBoolean(C.PLAYER_KEEP_CHAT_OPEN, false) || requireContext().prefs().getBoolean(C.CHAT_DISABLE, false)) {
            viewModel.start()
        }
    }

    companion object {
        private const val KEY_IS_LIVE = "isLive"
        private const val KEY_CHANNEL_ID = "channel_id"
        private const val KEY_CHANNEL_LOGIN = "channel_login"
        private const val KEY_CHANNEL_NAME = "channel_name"
        private const val KEY_VIDEO_ID = "videoId"
        private const val KEY_START_TIME_EMPTY = "startTime_empty"
        private const val KEY_START_TIME = "startTime"

        fun newInstance(channelId: String?, channelLogin: String?, channelName: String?) = ChatFragment().apply {
            arguments = Bundle().apply {
                putBoolean(KEY_IS_LIVE, true)
                putString(KEY_CHANNEL_ID, channelId)
                putString(KEY_CHANNEL_LOGIN, channelLogin)
                putString(KEY_CHANNEL_NAME, channelName)
            }
        }

        fun newInstance(channelId: String?, videoId: String?, startTime: Double?) = ChatFragment().apply {
            arguments = Bundle().apply {
                putBoolean(KEY_IS_LIVE, false)
                putString(KEY_CHANNEL_ID, channelId)
                putString(KEY_VIDEO_ID, videoId)
                if (startTime != null) {
                    putBoolean(KEY_START_TIME_EMPTY, false)
                    putDouble(KEY_START_TIME, startTime)
                } else {
                    putBoolean(KEY_START_TIME_EMPTY, true)
                }
            }
        }
    }
}