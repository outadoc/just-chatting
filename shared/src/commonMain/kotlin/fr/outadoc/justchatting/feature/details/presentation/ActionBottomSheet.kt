package fr.outadoc.justchatting.feature.details.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ActionBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    onDismissRequest: () -> Unit = {},
    header: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
    actions: (@Composable (PaddingValues) -> Unit)? = null,
) {
    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
    ) {
        DetailsDialogContent(
            modifier = Modifier.fillMaxWidth(),
            header = header,
            content = content,
            actions = actions,
        )
    }
}

@Composable
private fun DetailsDialogContent(
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
    actions: (@Composable (PaddingValues) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 32.dp),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier.padding(contentPadding),
        ) {
            header()
        }

        Box(
            modifier = Modifier.padding(contentPadding),
        ) {
            HorizontalDivider()
        }

        Box(
            modifier = Modifier.padding(contentPadding),
        ) {
            content()
        }

        if (actions != null) {
            Box(
                modifier = Modifier.padding(contentPadding),
            ) {
                HorizontalDivider()
            }

            Column(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                actions(contentPadding)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
