package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cake
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.created_at
import fr.outadoc.justchatting.utils.presentation.formatDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExtraUserInfo(
    modifier: Modifier = Modifier,
    user: User,
    showCreatedAt: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (user.description.isNotEmpty()) {
            Text(
                text = user.description,
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        if (showCreatedAt) {
            UserCreatedAt(user = user)
        }
    }
}

@Composable
internal fun UserCreatedAt(
    modifier: Modifier = Modifier,
    user: User,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            modifier = Modifier.size(16.dp),
            imageVector = Icons.Outlined.Cake,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(Res.string.created_at, user.createdAt.formatDate()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
