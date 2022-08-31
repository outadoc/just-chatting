package fr.outadoc.justchatting.ui.streams.followed

import android.os.Bundle
import android.view.View
import fr.outadoc.justchatting.ui.streams.BaseStreamsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class FollowedStreamsFragment : BaseStreamsFragment<FollowedStreamsViewModel>() {

    override val viewModel: FollowedStreamsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadStreams()
    }
}
