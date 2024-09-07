package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.icerock.moko.resources.compose.stringResource
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.utils.presentation.formatDate

@Composable
internal fun UserInfo(
    modifier: Modifier = Modifier,
    user: User,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AsyncImage(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .size(48.dp),
                model = user.profileImageUrl,
                contentDescription = null,
            )

            Text(
                text = user.displayName,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
            )
        }

        if (user.description.isNotEmpty()) {
            Text(text = user.description)
        }

        val createdAt = user.createdAt.formatDate()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 8.dp),
                imageVector = Icons.Default.Cake,
                contentDescription = null,
            )

            Text(
                text = stringResource(MR.strings.created_at, createdAt),
            )
        }
    }
}
