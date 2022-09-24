package fr.outadoc.justchatting.ui.view.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.databinding.DialogChatStreamInfoBinding
import fr.outadoc.justchatting.model.helix.stream.Stream
import fr.outadoc.justchatting.model.helix.user.User
import fr.outadoc.justchatting.ui.common.ExpandingBottomSheetDialogFragment
import fr.outadoc.justchatting.util.formatNumber
import fr.outadoc.justchatting.util.formatTime
import fr.outadoc.justchatting.util.formatTimestamp
import fr.outadoc.justchatting.util.loadImage
import kotlinx.datetime.toInstant
import org.koin.androidx.viewmodel.ext.android.viewModel

class StreamInfoDialog : ExpandingBottomSheetDialogFragment() {

    companion object {
        private const val KEY_USERID = "userid"

        fun newInstance(userId: String): StreamInfoDialog {
            return StreamInfoDialog().apply {
                arguments = bundleOf(KEY_USERID to userId)
            }
        }
    }

    private val viewModel: StreamInfoViewModel by viewModel()
    private var viewHolder: DialogChatStreamInfoBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewHolder = DialogChatStreamInfoBinding.inflate(inflater, container, false)
        return viewHolder?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = requireArguments().getString(KEY_USERID)!!

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                StreamInfoViewModel.State.Idle -> {}
                is StreamInfoViewModel.State.Error -> {
                    state.error.printStackTrace()
                }

                is StreamInfoViewModel.State.Loaded -> {
                    viewHolder?.apply {
                        updateUserLayout(state.user)
                        updateStreamLayout(state.stream)
                    }
                }

                StreamInfoViewModel.State.Loading -> {
                }
            }
        }

        viewModel.loadUser(channelId = userId)
    }

    private fun DialogChatStreamInfoBinding.updateUserLayout(user: User) {
        if (user.profileImageUrl != null) {
            userLayout.isVisible = true
            userImage.isVisible = true
            userImage.loadImage(user.profileImageUrl, circle = true)
        } else {
            userImage.isVisible = false
        }

        userLayout.isVisible = true
        userName.isVisible = true
        userName.text = user.displayName

        if (user.followersCount != null) {
            userLayout.isVisible = true
            userFollowers.isVisible = true
            userFollowers.text = requireContext().getString(
                R.string.followers,
                user.followersCount.formatNumber()
            )
        } else {
            userFollowers.isVisible = false
        }

        if (user.createdAt != null) {
            userLayout.isVisible = true
            userCreated.isVisible = true
            userCreated.text = requireContext().getString(
                R.string.created_at,
                user.createdAt.toInstant().formatTime(requireContext())
            )
        } else {
            userCreated.isVisible = false
        }
    }

    private fun DialogChatStreamInfoBinding.updateStreamLayout(stream: Stream?) {
        if (stream?.userName != null) {
            userName.isVisible = true
            userName.text = stream.userName
        } else {
            userName.isVisible = false
        }

        if (stream?.title != null) {
            streamTitle.isVisible = true
            streamTitle.text = stream.title
        } else {
            streamTitle.isVisible = false
        }

        if (stream?.gameName != null) {
            gameName.isVisible = true
            gameName.text = stream.gameName
        } else {
            gameName.isVisible = false
        }

        if (stream?.viewerCount != null) {
            viewers.isVisible = true
            viewers.text = context?.resources?.getQuantityString(
                R.plurals.viewers,
                stream.viewerCount,
                stream.viewerCount.formatNumber()
            )
        } else {
            viewers.isVisible = false
        }

        stream?.startedAt
            ?.toInstant()
            ?.formatTimestamp(requireContext()).let {
                if (it != null) {
                    uptime.isVisible = true
                    uptime.text = requireContext().getString(R.string.uptime, it)
                } else {
                    uptime.isVisible = false
                }
            }
    }
}
