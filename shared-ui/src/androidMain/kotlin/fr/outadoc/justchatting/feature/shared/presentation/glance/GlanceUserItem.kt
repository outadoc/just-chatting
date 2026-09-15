package fr.outadoc.justchatting.feature.shared.presentation.glance

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.height
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextDefaults
import fr.outadoc.justchatting.feature.shared.domain.model.User

@Composable
internal fun GlanceUserItem(
    modifier: GlanceModifier = GlanceModifier,
    user: User,
    profileImage: ImageProvider = rememberProfileImageProvider(user),
) {
    Column(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = GlanceModifier.size(48.dp),
            provider = profileImage,
            contentDescription = null,
        )

        Spacer(
            modifier = GlanceModifier.height(8.dp),
        )

        Text(
            text = user.displayName,
            style =
                TextDefaults.defaultTextStyle.copy(
                    color = GlanceTheme.colors.onSurfaceVariant,
                ),
            maxLines = 1,
        )
    }
}
