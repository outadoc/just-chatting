package fr.outadoc.justchatting.feature.onboarding.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.app_name
import fr.outadoc.justchatting.shared.onboarding_message
import fr.outadoc.justchatting.shared.onboarding_title
import fr.outadoc.justchatting.shared.presentation.icons.AppIcon
import fr.outadoc.justchatting.utils.presentation.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
) {
    Scaffold(modifier = modifier) { insets ->
        Column(
            modifier =
            Modifier
                .padding(insets)
                .padding(16.dp)
                .fillMaxSize()
                .widthIn(max = 320.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.widthIn(max = 320.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            modifier = Modifier.size(AppIconSize),
                            painter = rememberVectorPainter(AppIcon),
                            tint = MaterialTheme.colorScheme.onSurface,
                            contentDescription = null,
                        )

                        Text(
                            text =
                            buildAnnotatedString {
                                appendLine(stringResource(Res.string.onboarding_title))
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    appendLine(stringResource(Res.string.app_name))
                                }
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            text = stringResource(Res.string.onboarding_message),
                        )

                        SignInWithTwitchButton(
                            modifier = Modifier.padding(top = 24.dp),
                            onClick = onLoginClick,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
internal fun OnboardingScreenPreview() {
    AppTheme {
        OnboardingScreen(onLoginClick = {})
    }
}

private val AppIconSize = 80.dp
