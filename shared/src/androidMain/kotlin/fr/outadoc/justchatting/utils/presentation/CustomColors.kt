package fr.outadoc.justchatting.utils.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val darkColors = CustomColorScheme(
    success = Color(0xFF426900),
    onSuccess = Color(0xFFFFFFFF),
    successContainer = Color(0xFFB2F655),
    onSuccessContainer = Color(0xFF112000),
    live = Color(0xFFD93A3A),
)

private val lightColors = CustomColorScheme(
    success = Color(0xFF98D93A),
    onSuccess = Color(0xFF203600),
    successContainer = Color(0xFF314F00),
    onSuccessContainer = Color(0xFFB2F655),
    live = Color(0xFFD93A3A),
)

internal data class CustomColorScheme(
    val success: Color,
    val successContainer: Color,
    val onSuccess: Color,
    val onSuccessContainer: Color,
    val live: Color,
)

internal val MaterialTheme.customColors: CustomColorScheme
    @Composable
    get() = (if (colorScheme.isDark) darkColors else lightColors)
