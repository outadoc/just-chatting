package fr.outadoc.justchatting.feature.details.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DetailsDialog(
    modifier: Modifier = Modifier,
    userDetails: @Composable () -> Unit = {},
    streamDetails: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    onDismissRequest: () -> Unit = {},
) {
    ModalBottomSheet(
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
        ),
        onDismissRequest = onDismissRequest,
    ) {
        DetailsDialogContent(
            modifier = modifier
                .fillMaxWidth()
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 16.dp,
                ),
            userDetails = userDetails,
            streamDetails = streamDetails,
            actions = actions,
        )
    }
}

@Composable
internal fun DetailsDialogContent(
    modifier: Modifier = Modifier,
    userDetails: @Composable () -> Unit,
    streamDetails: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        userDetails()

        HorizontalDivider()

        streamDetails()

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions()
        }
    }
}
