package fr.outadoc.justchatting.ui.view.chat

import android.graphics.Color
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
import kotlinx.datetime.Instant
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
        } else {
            userImage.isVisible = false
        }

        if (user.display_name != null) {
            userLayout.isVisible = true
            userName.isVisible = true
            userName.text = user.display_name

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
    }

    private fun DialogChatStreamInfoBinding.updateStreamLayout(stream: Stream?) {
        if (stream?.viewer_count == null && stream?.lastBroadcast != null) {
            Instant.parse(stream.lastBroadcast)
                .formatTime(requireContext())
                .let {
                    lastBroadcast.text = context?.getString(R.string.last_broadcast_date, it)
                    lastBroadcast.isVisible = true
                }
        }

        val channelLogo = stream?.channelLogo
        if (!userImage.isVisible && channelLogo != null) {
            userImage.isVisible = true
            userImage.loadImage(channelLogo, circle = true)
        }

        if (stream?.user_name != null) {
            userName.isVisible = true
            userName.text = stream.user_name
        } else {
            userName.isVisible = false
        }

        if (stream?.game_name != null) {
            gameName.isVisible = true
            gameName.text = stream.game_name
        } else {
            gameName.isVisible = false
        }

        if (stream?.viewer_count != null) {
            viewers.isVisible = true
            viewers.text = context?.resources?.getQuantityString(
                R.plurals.viewers,
                stream.viewer_count,
                stream.viewer_count.formatNumber()
            )
        } else {
            viewers.isVisible = false
        }

        stream?.started_at
            ?.let { Instant.parse(it) }
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
