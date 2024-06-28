package fr.outadoc.justchatting.utils.ui

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    group = "themes",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
)
@Preview(
    group = "themes",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    showBackground = true,
)
internal annotation class ThemePreviews
