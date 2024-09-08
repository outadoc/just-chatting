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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit,
) {
    Scaffold(modifier = modifier) { insets ->
        Column(
            modifier = Modifier
                .padding(insets)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.widthIn(max = 320.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        modifier = Modifier
                            .size(AppIconSize)
                            .padding(bottom = 24.dp),
                        painter = painterResource(MR.images.ic_notif),
                        contentDescription = null,
                    )

                    Text(
                        text = stringResource(
                            MR.strings.onboarding_title,
                            stringResource(MR.strings.app_name),
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        modifier = Modifier.padding(vertical = 32.dp),
                        text = stringResource(MR.strings.onboarding_message),
                        textAlign = TextAlign.Center,
                    )

                    SignInWithTwitchButton(
                        onClick = onLoginClick,
                    )
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
