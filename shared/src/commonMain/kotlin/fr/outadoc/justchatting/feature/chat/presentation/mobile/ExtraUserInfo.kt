package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.shared.domain.model.User
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.created_at
import fr.outadoc.justchatting.utils.presentation.formatDate
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExtraUserInfo(
    modifier: Modifier = Modifier,
    user: User,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
                text = stringResource(Res.string.created_at, createdAt),
            )
        }
    }
}
