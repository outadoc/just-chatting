package fr.outadoc.justchatting.feature.shared.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import fr.outadoc.justchatting.feature.chat.presentation.ui.remoteImageModel

internal object UserAvatarDefaults {
    val LargeSize: Dp = 56.dp
    val LargeShape: Shape = RoundedCornerShape(16.dp)

    val MediumSize: Dp = 40.dp
    val MediumShape: Shape = RoundedCornerShape(12.dp)

    val SmallShape: Shape = CircleShape
}

@Composable
internal fun UserAvatar(
    modifier: Modifier = Modifier,
    profileImageUrl: String?,
    size: Dp = UserAvatarDefaults.MediumSize,
    shape: Shape = UserAvatarDefaults.MediumShape,
) {
    AsyncImage(
        modifier =
            modifier
                .size(size)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        model = remoteImageModel(profileImageUrl),
        contentDescription = null,
    )
}
