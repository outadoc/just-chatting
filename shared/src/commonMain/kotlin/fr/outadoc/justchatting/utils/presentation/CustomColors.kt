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
    fallbackChatColors = setOf(
        Color(0xFFEF5350),
        Color(0xFFEC407A),
        Color(0xFFAB47BC),
        Color(0xFF7E57C2),
        Color(0xFF5C6BC0),
        Color(0xFF42A5F5),
        Color(0xFF29B6F6),
        Color(0xFF26C6DA),
        Color(0xFF26A69A),
        Color(0xFF66BB6A),
        Color(0xFF9CCC65),
        Color(0xFFD4E157),
        Color(0xFFFFCA28),
        Color(0xFFFFA726),
        Color(0xFFFF7043),
    ),
)

private val lightColors = CustomColorScheme(
    success = Color(0xFF98D93A),
    onSuccess = Color(0xFF203600),
    successContainer = Color(0xFF314F00),
    onSuccessContainer = Color(0xFFB2F655),
    live = Color(0xFFD93A3A),
    fallbackChatColors = setOf(
        Color(0xffC62828),
        Color(0xffAD1457),
        Color(0xff6A1B9A),
        Color(0xff4527A0),
        Color(0xff283593),
        Color(0xff1565C0),
        Color(0xff0277BD),
        Color(0xff00838F),
        Color(0xff00695C),
        Color(0xff2E7D32),
        Color(0xff558B2F),
        Color(0xff9E9D24),
        Color(0xffF9A825),
        Color(0xffFF8F00),
        Color(0xffEF6C00),
    ),
)

internal data class CustomColorScheme(
    val success: Color,
    val successContainer: Color,
    val onSuccess: Color,
    val onSuccessContainer: Color,
    val live: Color,
    val fallbackChatColors: Set<Color>,
)

internal val MaterialTheme.customColors: CustomColorScheme
    @Composable
    get() = (if (colorScheme.isDark) darkColors else lightColors)
