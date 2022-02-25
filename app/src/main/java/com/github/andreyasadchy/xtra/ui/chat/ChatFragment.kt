package com.github.andreyasadchy.xtra.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
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

    var chLogin: String? = null
    var chName: String? = null
    override fun initialize() {
        val args = requireArguments()
        val channelId = args.getString(KEY_CHANNEL)
        val user = User.get(requireContext())
        val userIsLoggedIn = user is LoggedIn
        val useHelix = requireContext().prefs().getBoolean(C.API_USEHELIX, true) && userIsLoggedIn
        val helixClientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, "")
        val gqlClientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, "") ?: ""
        val showUserNotice = requireContext().prefs().getBoolean(C.CHAT_SHOW_USERNOTICE, true)
        val showClearMsg = requireContext().prefs().getBoolean(C.CHAT_SHOW_CLEARMSG, true)
        val showClearChat = requireContext().prefs().getBoolean(C.CHAT_SHOW_CLEARCHAT, true)
        val enableRecentMsg = requireContext().prefs().getBoolean(C.CHAT_RECENT, true)
        val recentMsgLimit = requireContext().prefs().getInt(C.CHAT_RECENT_LIMIT, 100)
        val isLive = args.getBoolean(KEY_IS_LIVE)
        val enableChat = if (isLive) {
            viewModel.startLive(user, useHelix, helixClientId, gqlClientId, channelId, chLogin, chName, showUserNotice, showClearMsg, showClearChat, enableRecentMsg, recentMsgLimit.toString())
            chatView.init(this)
            chatView.setCallback(viewModel)
            if (userIsLoggedIn) {
                chatView.setUsername(user.name)
                chatView.setChatters(viewModel.chatters)
                val emotesObserver = Observer(chatView::addEmotes)
                viewModel.emotesFromSets.observe(viewLifecycleOwner, emotesObserver)
                viewModel.recentEmotes.observe(viewLifecycleOwner, emotesObserver)
                viewModel.newChatter.observe(viewLifecycleOwner, Observer(chatView::addChatter))
            }
            true
        } else {
            args.getString(KEY_VIDEO_ID).let {
                if (it != null) {
                    chatView.init(this)
                    val getCurrentPosition = (parentFragment as ChatReplayPlayerFragment)::getCurrentPosition
                    viewModel.startReplay(user, useHelix, helixClientId, gqlClientId, channelId, it, args.getDouble(KEY_START_TIME), getCurrentPosition)
                    true
                } else {
                    chatView.chatReplayUnavailable.visible()
                    false
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

    override fun onViewProfileClicked(id: String?, login: String?, name: String?, profileImage: String?) {
        (requireActivity() as MainActivity).viewChannel(id, login, name, profileImage)
        (parentFragment as? BasePlayerFragment)?.minimize()
    }

    override fun onNetworkRestored() {
        if (isResumed) {
            viewModel.start()
        }
    }

    override fun onMovedToBackground() {
        viewModel.stop()
    }

    override fun onMovedToForeground() {
        viewModel.start()
    }

    companion object {
        private const val KEY_IS_LIVE = "isLive"
        private const val KEY_CHANNEL = "channel"
        private const val KEY_VIDEO_ID = "videoId"
        private const val KEY_START_TIME = "startTime"

        fun newInstance(channelId: String?, channelLogin: String?, channelName: String?) = ChatFragment().apply {
            arguments = bundleOf(KEY_IS_LIVE to true, KEY_CHANNEL to channelId); chLogin = channelLogin ; chName = channelName
        }

        fun newInstance(channelId: String?, videoId: String?, startTime: Double?) = ChatFragment().apply {
            arguments = bundleOf(KEY_IS_LIVE to false, KEY_CHANNEL to channelId, KEY_VIDEO_ID to videoId, KEY_START_TIME to startTime)
        }
    }
}