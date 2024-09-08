package fr.outadoc.justchatting.feature.onboarding.presentation.mobile

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.shared.MR
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun SignInWithTwitchButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = TwitchBrandColor,
            contentColor = Color.White,
        ),
    ) {
        Icon(
            painterResource(MR.images.ic_twitch),
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )

        Spacer(Modifier.size(ButtonDefaults.IconSpacing))

        Text(
            text = stringResource(MR.strings.onboarding_login_action),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview
@Composable
internal fun SignInWithTwitchButtonPreview() {
    SignInWithTwitchButton(onClick = {})
}

private val TwitchBrandColor = Color(0xFF7718AD)
