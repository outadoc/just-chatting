package fr.outadoc.justchatting.ui.view.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.DialogChatMessageClickBinding
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.common.ExpandingBottomSheetDialogFragment
import fr.outadoc.justchatting.util.formatNumber
import fr.outadoc.justchatting.util.formatTime
import fr.outadoc.justchatting.util.loadImage
import kotlinx.datetime.Instant
import org.koin.android.ext.android.inject

class MessageClickedDialog : ExpandingBottomSheetDialogFragment() {

    interface OnButtonClickListener {
        fun onReplyClicked(userName: String)
        fun onCopyMessageClicked(message: String)
        fun onViewProfileClicked(id: String?, login: String?, name: String?, channelLogo: String?)
    }

    companion object {
        private const val KEY_ORIGINAL = "original"
        private const val KEY_FORMATTED = "formatted"
        private const val KEY_USERID = "userid"

        private val savedUsers = mutableListOf<User>()

        fun newInstance(
            originalMessage: CharSequence,
            formattedMessage: CharSequence,
            userId: String?
        ) = MessageClickedDialog().apply {
            arguments = bundleOf(
                KEY_ORIGINAL to originalMessage,
                KEY_FORMATTED to formattedMessage,
                KEY_USERID to userId
            )
        }
    }

    private lateinit var listener: OnButtonClickListener

    private val viewModel: MessageClickedViewModel by inject()
    private var viewHolder: DialogChatMessageClickBinding? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as OnButtonClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewHolder = DialogChatMessageClickBinding.inflate(inflater, container, false)
        return viewHolder?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()

        val formattedMsg = args.getCharSequence(KEY_FORMATTED)!!
        val originalMsg = args.getCharSequence(KEY_ORIGINAL)!!
        val userId = args.getString(KEY_USERID)

        viewHolder?.apply {
            message.text = formattedMsg

            if (userId != null) {
                val item = savedUsers.find { it.id == userId }
                if (item != null) {
                    updateUserLayout(item)
                } else {
                    viewModel.loadUser(channelId = userId)
                        .observe(viewLifecycleOwner) { user ->
                            if (user != null) {
                                savedUsers.add(user)
                                updateUserLayout(user)
                            } else {
                                viewProfile.isVisible = true
                            }
                        }
                }

                copyMessage.setOnClickListener {
                    listener.onCopyMessageClicked(originalMsg.substring(originalMsg.indexOf(':') + 2))
                    dismiss()
                }

                reply.isVisible = true
                copyMessage.isVisible = true
            }

            copyClip.setOnClickListener {
                val clipboard = requireContext().getSystemService<ClipboardManager>()
                clipboard?.setPrimaryClip(
                    ClipData.newPlainText(
                        "label",
                        if (userId != null) {
                            originalMsg.substring(originalMsg.indexOf(':') + 2)
                        } else {
                            originalMsg
                        }
                    )
                )

                dismiss()
            }
        }
    }

    private fun DialogChatMessageClickBinding.updateUserLayout(user: User) {
        if (user.bannerImageURL != null) {
            userLayout.isVisible = true
            bannerImage.isVisible = true
            bannerImage.loadImage(user.bannerImageURL)
        } else {
            bannerImage.isVisible = false
        }

        if (user.channelLogo != null) {
            userLayout.isVisible = true
            userImage.isVisible = true
            userImage.loadImage(user.channelLogo, circle = true)
            userImage.setOnClickListener {
                listener.onViewProfileClicked(
                    user.id,
                    user.login,
                    user.display_name,
                    user.channelLogo
                )
                dismiss()
            }
        } else {
            userImage.isVisible = false
        }

        if (user.display_name != null) {
            userLayout.isVisible = true
            userName.isVisible = true
            userName.text = user.display_name
            userName.setOnClickListener {
                listener.onViewProfileClicked(
                    user.id,
                    user.login,
                    user.display_name,
                    user.channelLogo
                )
                dismiss()
            }

            if (user.bannerImageURL != null) {
                userName.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            }
        } else {
            userName.isVisible = false
        }

        if (user.followers_count != null) {
            userLayout.isVisible = true
            userFollowers.isVisible = true
            userFollowers.text = requireContext().getString(
                R.string.followers,
                user.followers_count.formatNumber()
            )

            if (user.bannerImageURL != null) {
                userFollowers.setTextColor(Color.LTGRAY)
                userFollowers.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            }
        } else {
            userFollowers.isVisible = false
        }

        if (user.created_at != null) {
            userLayout.isVisible = true
            userCreated.isVisible = true
            userCreated.text = requireContext().getString(
                R.string.created_at,
                Instant.parse(user.created_at).formatTime(requireContext())
            )

            if (user.bannerImageURL != null) {
                userCreated.setTextColor(Color.LTGRAY)
                userCreated.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            }
        } else {
            userCreated.isVisible = false
        }

        if (user.login != null) {
            reply.isVisible = true
            reply.setOnClickListener {
                listener.onReplyClicked(user.login)
                dismiss()
            }
        } else {
            reply.isVisible = false
        }

        if (!userImage.isVisible && !userName.isVisible) {
            viewProfile.isVisible = true
        }
    }
}
