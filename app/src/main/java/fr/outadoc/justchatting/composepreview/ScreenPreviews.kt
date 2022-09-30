package fr.outadoc.justchatting.composepreview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    group = "themes",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    showSystemUi = true
)
@Preview(
    group = "themes",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showSystemUi = true
)
annotation class ScreenPreviews
