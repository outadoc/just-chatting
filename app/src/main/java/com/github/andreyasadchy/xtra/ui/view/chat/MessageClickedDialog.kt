package com.github.andreyasadchy.xtra.ui.view.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment
import com.github.andreyasadchy.xtra.util.*
import kotlinx.android.synthetic.main.dialog_chat_message_click.*
import javax.inject.Inject

class MessageClickedDialog : ExpandingBottomSheetDialogFragment(), Injectable {

    interface OnButtonClickListener {
        fun onReplyClicked(userName: String)
        fun onCopyMessageClicked(message: String)
        fun onViewProfileClicked(id: String?, login: String?, name: String?, profileImage: String?)
    }

    companion object {
        private const val KEY_MESSAGING = "messaging"
        private const val KEY_ORIGINAL = "original"
        private const val KEY_FORMATTED = "formatted"
        private const val KEY_USERID = "userid"
        private val savedUsers = mutableListOf<User>()

        fun newInstance(messagingEnabled: Boolean, originalMessage: CharSequence, formattedMessage: CharSequence, userId: String?) = MessageClickedDialog().apply {
            arguments = bundleOf(KEY_MESSAGING to messagingEnabled, KEY_ORIGINAL to originalMessage, KEY_FORMATTED to formattedMessage, KEY_USERID to userId)
        }
    }

    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel by viewModels<MessageClickedViewModel> { viewModelFactory }

    private lateinit var listener: OnButtonClickListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnButtonClickListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_chat_message_click, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        message.text = args.getCharSequence(KEY_FORMATTED)!!
        val msg = args.getCharSequence(KEY_ORIGINAL)!!
        val userId = args.getString(KEY_USERID)
        val clipboard = getSystemService(requireContext(), ClipboardManager::class.java)
        val useHelix = requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != ""
        if (userId != null) {
            val item = savedUsers.find { it.id == userId }
            if (item != null) {
                updateUserLayout(item)
            } else {
                viewModel.loadUser(useHelix = useHelix, clientId = requireContext().prefs().getString(if (useHelix) C.HELIX_CLIENT_ID else C.GQL_CLIENT_ID, ""),
                    token = if (useHelix) requireContext().prefs().getString(C.TOKEN, "") else null, channelId = userId).observe(viewLifecycleOwner) { user ->
                    if (user != null) {
                        savedUsers.add(user)
                        updateUserLayout(user)
                    }
                }
            }
            if (args.getBoolean(KEY_MESSAGING)) {
                reply.visible()
                reply.setOnClickListener {
                    listener.onReplyClicked(extractUserName(msg))
                    dismiss()
                }
                copyMessage.visible()
                copyMessage.setOnClickListener {
                    listener.onCopyMessageClicked(msg.substring(msg.indexOf(':') + 2))
                    dismiss()
                }
            }
        }
        copyClip.setOnClickListener {
            clipboard?.setPrimaryClip(ClipData.newPlainText("label", if (userId != null) msg.substring(msg.indexOf(':') + 2) else msg))
            dismiss()
        }
    }

    private fun updateUserLayout(user: User) {
        if (user.bannerImageURL != null) {
            userLayout.visible()
            bannerImage.visible()
            bannerImage.loadImage(requireParentFragment(), user.bannerImageURL)
        }
        if (user.channelLogo != null) {
            userLayout.visible()
            userImage.visible()
            userImage.loadImage(requireParentFragment(), user.channelLogo, circle = true)
            userImage.setOnClickListener {
                listener.onViewProfileClicked(user.id, user.login, user.display_name, user.channelLogo)
                dismiss()
            }
        }
        if (user.display_name != null) {
            userLayout.visible()
            userName.visible()
            userName.text = user.display_name
            userName.setOnClickListener {
                listener.onViewProfileClicked(user.id, user.login, user.display_name, user.channelLogo)
                dismiss()
            }
            if (user.bannerImageURL != null) {
                userName.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            }
        }
        if (user.followers_count != null) {
            userLayout.visible()
            userFollowers.visible()
            userFollowers.text = requireContext().getString(R.string.followers, TwitchApiHelper.formatCount(user.followers_count, requireContext().prefs().getBoolean(C.UI_TRUNCATEVIEWCOUNT, false)))
            if (user.bannerImageURL != null) {
                userFollowers.setTextColor(Color.LTGRAY)
                userFollowers.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            }
        }
        if (user.created_at != null) {
            userLayout.visible()
            userCreated.visible()
            userCreated.text = requireContext().getString(R.string.created_at, TwitchApiHelper.formatTimeString(requireContext(), user.created_at))
            if (user.bannerImageURL != null) {
                userCreated.setTextColor(Color.LTGRAY)
                userCreated.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            }
        }
    }

    private fun extractUserName(text: CharSequence): String {
        val userName = StringBuilder()
        for (c in text) {
            if (!c.isWhitespace()) {
                if (c != ':') {
                    userName.append(c)
                } else {
                    break
                }
            }
        }
        return userName.toString()
    }
}