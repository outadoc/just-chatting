package fr.outadoc.justchatting.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter3.Mdc3Theme
import fr.outadoc.justchatting.R
import fr.outadoc.justchatting.composepreview.ScreenPreviews

private val AppIconSize = 80.dp

@ScreenPreviews
@Composable
fun OnboardingScreenPreview() {
    Mdc3Theme {
        OnboardingScreen(onLoginClick = {})
    }
}

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    modifier = Modifier
                        .size(AppIconSize)
                        .padding(bottom = 24.dp),
                    painter = painterResource(R.drawable.ic_notif),
                    contentDescription = null
                )

                Text(
                    text = stringResource(
                        R.string.onboarding_title,
                        stringResource(R.string.app_name)
                    ),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Text(
                    modifier = Modifier.padding(vertical = 32.dp),
                    text = stringResource(R.string.onboarding_message),
                    textAlign = TextAlign.Center
                )

                SignInWithTwitchButton(
                    onClick = onLoginClick
                )
            }
        }
    }
}
