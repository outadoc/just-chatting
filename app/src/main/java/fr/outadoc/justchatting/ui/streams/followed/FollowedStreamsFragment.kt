package fr.outadoc.justchatting.ui.streams.followed

import fr.outadoc.justchatting.ui.streams.BaseStreamsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class FollowedStreamsFragment : BaseStreamsFragment<FollowedStreamsViewModel>() {

    override val viewModel: FollowedStreamsViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        viewModel.loadStreams()
    }
}
