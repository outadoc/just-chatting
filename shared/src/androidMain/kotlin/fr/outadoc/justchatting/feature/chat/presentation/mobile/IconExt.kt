package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector
import fr.outadoc.justchatting.component.chatapi.common.Icon

fun Icon.toMaterialIcon(): ImageVector = when (this) {
    Icon.Bolt -> Icons.Default.Bolt
    Icon.CallReceived -> Icons.Default.CallReceived
    Icon.Campaign -> Icons.Default.Campaign
    Icon.Cancel -> Icons.Default.Cancel
    Icon.FastForward -> Icons.Default.FastForward
    Icon.Gavel -> Icons.Default.Gavel
    Icon.Highlight -> Icons.Default.Highlight
    Icon.Redeem -> Icons.Default.Redeem
    Icon.Reply -> Icons.Default.Reply
    Icon.Send -> Icons.Default.Send
    Icon.Star -> Icons.Outlined.Star
    Icon.Toll -> Icons.Default.Toll
    Icon.VolunteerActivism -> Icons.Default.VolunteerActivism
    Icon.WavingHand -> Icons.Default.WavingHand
}
