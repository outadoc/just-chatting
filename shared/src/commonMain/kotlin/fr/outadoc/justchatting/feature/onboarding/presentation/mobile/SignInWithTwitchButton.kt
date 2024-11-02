package fr.outadoc.justchatting.feature.onboarding.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.onboarding_login_action
import fr.outadoc.justchatting.shared.presentation.icons.TwitchIcon
import org.jetbrains.compose.resources.stringResource
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
        Row(
            horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.IconSpacing),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                rememberVectorPainter(TwitchIcon),
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )

            Text(
                text = stringResource(Res.string.onboarding_login_action),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview
@Composable
internal fun SignInWithTwitchButtonPreview() {
    SignInWithTwitchButton(onClick = {})
}

private val TwitchBrandColor = Color(0xFF7718AD)
