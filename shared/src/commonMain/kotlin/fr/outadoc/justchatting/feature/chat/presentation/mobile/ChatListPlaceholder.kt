package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.shared.presentation.mobile.placeholder.core.PlaceholderHighlight
import fr.outadoc.justchatting.feature.shared.presentation.mobile.placeholder.material3.placeholder
import fr.outadoc.justchatting.feature.shared.presentation.mobile.placeholder.material3.shimmer
import fr.outadoc.justchatting.utils.core.isOdd
import fr.outadoc.justchatting.utils.presentation.AppTheme
import kotlin.random.Random

@Composable
internal fun ChatListPlaceholder(
    modifier: Modifier = Modifier,
    placeholderItemCount: Int = 100,
) {
    val random = Random(seed = 0xbadcafe)
    val listState = rememberLazyListState()

    LazyColumn(
        modifier = modifier,
        userScrollEnabled = false,
        reverseLayout = true,
        state = listState,
    ) {
        items(
            count = placeholderItemCount,
        ) { index ->
            Box(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        if (index.isOdd) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
            ) {
                Box(
                    modifier =
                    Modifier
                        .padding(
                            horizontal = 8.dp,
                            vertical = 10.dp,
                        ).height(16.dp)
                        .fillMaxSize(
                            fraction =
                            random
                                .nextDouble(
                                    from = 0.45,
                                    until = 0.9,
                                ).toFloat(),
                        ).placeholder(
                            visible = true,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            highlight = PlaceholderHighlight.shimmer(),
                        ),
                )
            }
        }
    }
}

@Preview
@Composable
internal fun ChatListPlaceholderPreview() {
    AppTheme {
        Surface {
            ChatListPlaceholder()
        }
    }
}
