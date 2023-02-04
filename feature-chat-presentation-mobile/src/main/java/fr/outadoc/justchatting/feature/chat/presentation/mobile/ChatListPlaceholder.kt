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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import fr.outadoc.justchatting.utils.core.isOdd
import fr.outadoc.justchatting.utils.ui.AppTheme
import fr.outadoc.justchatting.utils.ui.ThemePreviews
import kotlin.random.Random

@ThemePreviews
@Composable
fun ChatListPlaceholderPreview() {
    AppTheme {
        Surface {
            ChatListPlaceholder()
        }
    }
}

@Composable
fun ChatListPlaceholder(
    modifier: Modifier = Modifier,
    placeholderItemCount: Int = 100
) {
    val random = remember { Random(seed = 0xbadcafe) }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = placeholderItemCount - 1
    )

    LazyColumn(
        modifier = modifier,
        userScrollEnabled = false,
        state = listState
    ) {
        items(
            count = placeholderItemCount,
            contentType = { Any() }
        ) { index ->
            Box(
                modifier = Modifier
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
                    modifier = Modifier
                        .padding(
                            horizontal = 8.dp,
                            vertical = 10.dp,
                        )
                        .height(16.dp)
                        .fillMaxSize(
                            fraction = random
                                .nextDouble(
                                    from = 0.45,
                                    until = 0.9,
                                )
                                .toFloat(),
                        )
                        .placeholder(
                            visible = true,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            highlight = PlaceholderHighlight.shimmer(),
                        ),
                )
            }
        }
    }
}
