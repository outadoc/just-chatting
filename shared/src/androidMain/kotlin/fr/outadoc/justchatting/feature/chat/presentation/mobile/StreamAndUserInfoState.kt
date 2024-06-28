package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.StreamAndUserInfoViewModel
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.ui.ThemePreviews

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
            StreamAndUserInfoViewModel.State.Initial -> {}
            is StreamAndUserInfoViewModel.State.Loading -> {
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }

            is StreamAndUserInfoViewModel.State.Error -> {
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(MR.strings.info_loadError),
                        style = MaterialTheme.typography.titleMedium,
                    )
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

@ThemePreviews
@Composable
private fun StreamAndUserInfoError() {
    StreamAndUserInfoState(
        state = StreamAndUserInfoViewModel.State.Error(null),
    )
}

@ThemePreviews
@Composable
private fun StreamAndUserInfoInitial() {
    StreamAndUserInfoState(
        state = StreamAndUserInfoViewModel.State.Initial,
    )
}
