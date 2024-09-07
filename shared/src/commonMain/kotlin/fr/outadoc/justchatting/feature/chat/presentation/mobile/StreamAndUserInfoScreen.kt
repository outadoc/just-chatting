package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.chat.presentation.StreamAndUserInfoViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun StreamAndUserInfoScreen(
    modifier: Modifier = Modifier,
    userId: String,
) {
    val viewModel: StreamAndUserInfoViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(userId) {
        viewModel.load(userId)
    }

    StreamAndUserInfoState(
        modifier = modifier,
        state = state,
    )
}
