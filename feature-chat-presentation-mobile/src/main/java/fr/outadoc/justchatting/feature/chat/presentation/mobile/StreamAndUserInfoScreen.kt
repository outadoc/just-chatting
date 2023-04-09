package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.chat.presentation.StreamAndUserInfoViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun StreamAndUserInfoScreen(
    modifier: Modifier = Modifier,
    userLogin: String,
) {
    val viewModel: StreamAndUserInfoViewModel = getViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(userLogin) {
        viewModel.loadFromLogin(userLogin)
    }

    StreamAndUserInfoState(
        modifier = modifier,
        state = state,
    )
}
