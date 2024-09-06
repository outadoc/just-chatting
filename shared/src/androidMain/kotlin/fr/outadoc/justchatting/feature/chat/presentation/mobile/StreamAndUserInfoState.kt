package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.chat.presentation.StreamAndUserInfoViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun StreamAndUserInfoState(
    modifier: Modifier = Modifier,
    state: StreamAndUserInfoViewModel.State,
) {
    Crossfade(
        targetState = state,
        label = "bottom sheet state changes",
    ) { currentState ->
        when (currentState) {
            is StreamAndUserInfoViewModel.State.Loading -> {
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }

            is StreamAndUserInfoViewModel.State.Loaded -> {
                StreamAndUserInfo(
                    modifier = modifier,
                    user = currentState.user,
                    stream = currentState.stream,
                )
            }
        }
    }
}

@Preview
@Composable
private fun StreamAndUserInfoInitial() {
    StreamAndUserInfoState(
        state = StreamAndUserInfoViewModel.State.Loading,
    )
}
