package com.github.andreyasadchy.xtra.ui.view.chat

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.github.andreyasadchy.xtra.R
import com.github.andreyasadchy.xtra.di.Injectable
import com.github.andreyasadchy.xtra.model.helix.stream.Stream
import com.github.andreyasadchy.xtra.model.helix.user.User
import com.github.andreyasadchy.xtra.ui.common.ExpandingBottomSheetDialogFragment
import com.github.andreyasadchy.xtra.util.C
import com.github.andreyasadchy.xtra.util.TwitchApiHelper
import com.github.andreyasadchy.xtra.util.formatTime
import com.github.andreyasadchy.xtra.util.formatTimestamp
import com.github.andreyasadchy.xtra.util.loadImage
import kotlinx.android.synthetic.main.dialog_chat_message_click.bannerImage
import kotlinx.android.synthetic.main.dialog_chat_stream_info.gameName
import kotlinx.android.synthetic.main.dialog_chat_stream_info.lastBroadcast
import kotlinx.android.synthetic.main.dialog_chat_stream_info.uptime
import kotlinx.android.synthetic.main.dialog_chat_stream_info.userCreated
import kotlinx.android.synthetic.main.dialog_chat_stream_info.userFollowers
import kotlinx.android.synthetic.main.dialog_chat_stream_info.userImage
import kotlinx.android.synthetic.main.dialog_chat_stream_info.userLayout
import kotlinx.android.synthetic.main.dialog_chat_stream_info.userName
import kotlinx.android.synthetic.main.dialog_chat_stream_info.viewers
import kotlinx.datetime.Instant
import javax.inject.Inject

class StreamInfoDialog : ExpandingBottomSheetDialogFragment(), Injectable {

    companion object {
        private const val KEY_USERID = "userid"

        fun newInstance(userId: String): StreamInfoDialog {
            return StreamInfoDialog().apply {
                arguments = bundleOf(KEY_USERID to userId)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<StreamInfoViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_chat_stream_info, container, false)
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
                    updateUserLayout(state.user)
                    updateStreamLayout(state.stream)
                }
                StreamInfoViewModel.State.Loading -> {
                }
            }
        }

        viewModel.loadUser(channelId = userId)
    }

    private fun updateUserLayout(user: User) {
        if (user.bannerImageURL != null) {
            userLayout.isVisible = true
            bannerImage.isVisible = true
            bannerImage.loadImage(requireContext(), user.bannerImageURL)
        } else {
            bannerImage.isVisible = false
        }

        if (user.channelLogo != null) {
            userLayout.isVisible = true
            userImage.isVisible = true
            userImage.loadImage(requireContext(), user.channelLogo, circle = true)
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
                TwitchApiHelper.formatCount(user.followers_count)
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

    private fun updateStreamLayout(stream: Stream?) {
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
            userImage.loadImage(requireContext(), channelLogo, circle = true)
            requireArguments().putString(C.CHANNEL_PROFILEIMAGE, channelLogo)
        }

        stream?.user_name.let {
            if (it != null && it != requireArguments().getString(C.CHANNEL_DISPLAYNAME)) {
                userName.text = it
                requireArguments().putString(C.CHANNEL_DISPLAYNAME, it)
            }
        }

        stream?.user_login.let {
            if (it != null && it != requireArguments().getString(C.CHANNEL_LOGIN)) {
                requireArguments().putString(C.CHANNEL_LOGIN, it)
            }
        }

        if (stream?.game_name != null) {
            gameName.isVisible = true
            gameName.text = stream.game_name
        } else {
            gameName.isVisible = false
        }

        if (stream?.viewer_count != null) {
            viewers.isVisible = true
            viewers.text = TwitchApiHelper.formatViewersCount(requireContext(), stream.viewer_count)
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
