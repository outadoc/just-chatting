package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import fr.outadoc.justchatting.feature.chat.presentation.UserInfoViewModel
import fr.outadoc.justchatting.feature.details.presentation.ActionBottomSheet
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.info_loadError
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun UserInfoDialog(
    modifier: Modifier = Modifier,
    userId: String,
    onDismissRequest: () -> Unit = {},
) {
    val viewModel: UserInfoViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(userId) {
        viewModel.load(userId)
    }

    val sheetState = rememberModalBottomSheetState()

    when (val currentState = state) {
        is UserInfoViewModel.State.Loading -> {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = onDismissRequest,
            ) {
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        is UserInfoViewModel.State.Loaded -> {
            ActionBottomSheet(
                sheetState = sheetState,
                onDismissRequest = onDismissRequest,
                header = {
                    BasicUserInfo(user = currentState.user)
                },
                content = {
                    ExtraUserInfo(user = currentState.user)
                },
            )
        }

        is UserInfoViewModel.State.Error -> {
            ModalBottomSheet(
                sheetState = sheetState,
                onDismissRequest = onDismissRequest,
            ) {
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(Res.string.info_loadError))
                }
            }
        }
    }
}
