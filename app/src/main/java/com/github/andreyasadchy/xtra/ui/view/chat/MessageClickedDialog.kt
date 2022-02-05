package com.github.andreyasadchy.xtra.ui.view.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.gone
import com.github.andreyasadchy.xtra.util.prefs
import com.github.andreyasadchy.xtra.util.shortToast
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
        if (args.getBoolean(KEY_MESSAGING)) {
            if (userId != null) {
                reply.setOnClickListener {
                    listener.onReplyClicked(extractUserName(msg))
                    dismiss()
                }
                copyMessage.setOnClickListener {
                    listener.onCopyMessageClicked(msg.substring(msg.indexOf(':') + 2))
                    dismiss()
                }
            } else {
                reply.gone()
                copyMessage.gone()
            }
        } else {
            reply.gone()
            copyMessage.gone()
        }
        copyClip.setOnClickListener {
            clipboard?.setPrimaryClip(ClipData.newPlainText("label", if (userId != null) msg.substring(msg.indexOf(':') + 1) else msg))
            dismiss()
        }
        if (userId != null) {
            viewProfile.setOnClickListener {
                if (requireContext().prefs().getBoolean(C.API_USEHELIX, true) && requireContext().prefs().getString(C.USERNAME, "") != "") {
                    viewModel.loadUser(useHelix = true, clientId = requireContext().prefs().getString(C.HELIX_CLIENT_ID, ""), token = requireContext().prefs().getString(C.TOKEN, ""), channelId = userId).observe(viewLifecycleOwner) {
                        if (it != null) {
                            listener.onViewProfileClicked(it.id, it.login, it.display_name, it.channelLogo)
                        }
                        dismiss()
                    }
                } else {
                    viewModel.loadUser(useHelix = false, clientId = requireContext().prefs().getString(C.GQL_CLIENT_ID, ""), channelId = userId).observe(viewLifecycleOwner) {
                        if (it != null) {
                            listener.onViewProfileClicked(it.id, it.login, it.display_name, it.channelLogo)
                        }
                        dismiss()
                    }
                }
            }
        } else viewProfile.gone()
        viewModel.errors.observe(viewLifecycleOwner, Observer {
            requireContext().shortToast(R.string.error_loading_user)
        })
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