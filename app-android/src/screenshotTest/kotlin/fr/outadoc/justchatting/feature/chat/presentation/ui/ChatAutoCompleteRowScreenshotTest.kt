package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.presentation.AutoCompleteItem
import fr.outadoc.justchatting.preview.PreviewFixtures
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun ChatAutoCompleteRowScreenshotTest() {
    AppTheme {
        val items =
            listOf(
                AutoCompleteItem.User(PreviewFixtures.sampleChatter),
                AutoCompleteItem.User(
                    Chatter(
                        id = "2",
                        displayName = "HortyUnderscore",
                        login = "hortyunderscore",
                    ),
                ),
            )

        ChatAutoCompleteRow(
            onChatterClick = {},
            onEmoteClick = {},
            items = items,
        )
    }
}
