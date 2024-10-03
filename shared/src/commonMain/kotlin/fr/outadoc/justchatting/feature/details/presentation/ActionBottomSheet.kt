package fr.outadoc.justchatting.feature.details.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
internal fun ActionBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit = {},
    header: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
    actions: (LazyGridScope.() -> Unit)? = null,
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
            header = header,
            content = content,
            actions = actions,
        )
    }
}

@Composable
internal fun DetailsDialogContent(
    modifier: Modifier = Modifier,
    header: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit = {},
    actions: (LazyGridScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        header()

        HorizontalDivider()

        content()

        if (actions != null) {
            HorizontalDivider()

            LazyVerticalGrid(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                columns = GridCells.Adaptive(80.dp),
            ) {
                actions()
            }
        }
    }
}
