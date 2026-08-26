package fr.outadoc.justchatting

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import fr.outadoc.justchatting.feature.shared.presentation.LabelChip
import fr.outadoc.justchatting.utils.presentation.AppTheme

@PreviewTest
@Preview
@Composable
internal fun LabelChipScreenshotTest() {
    AppTheme {
        Surface {
            LabelChip {
                Text("Lorem ipsum.")
            }
        }
    }
}
