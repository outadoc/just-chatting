package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.icons.filled.Token
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector
import fr.outadoc.justchatting.component.chatapi.common.Icon

fun Icon.toMaterialIcon(): ImageVector = when (this) {
    Icon.AccountTree -> Icons.Default.AccountTree
    Icon.ArrowDownward -> Icons.Default.ArrowDownward
    Icon.ArrowDropDown -> Icons.Default.ArrowDropDown
    Icon.ArrowDropUp -> Icons.Default.ArrowDropUp
    Icon.ArrowForward -> Icons.Default.ArrowForward
    Icon.Bolt -> Icons.Default.Bolt
    Icon.Cake -> Icons.Default.Cake
    Icon.CallReceived -> Icons.Default.CallReceived
    Icon.Campaign -> Icons.Default.Campaign
    Icon.Cancel -> Icons.Default.Cancel
    Icon.CheckCircle -> Icons.Default.CheckCircle
    Icon.Clear -> Icons.Default.Clear
    Icon.FastForward -> Icons.Default.FastForward
    Icon.Gamepad -> Icons.Default.Gamepad
    Icon.Gavel -> Icons.Default.Gavel
    Icon.Highlight -> Icons.Default.Highlight
    Icon.LiveTv -> Icons.Default.LiveTv
    Icon.Mood -> Icons.Default.Mood
    Icon.OpenInNew -> Icons.Default.OpenInNew
    Icon.Person -> Icons.Default.Person
    Icon.PictureInPictureAlt -> Icons.Default.PictureInPictureAlt
    Icon.Redeem -> Icons.Default.Redeem
    Icon.Reply -> Icons.Default.Reply
    Icon.Send -> Icons.Default.Send
    Icon.Settings -> Icons.Default.Settings
    Icon.Share -> Icons.Default.Share
    Icon.Star -> Icons.Outlined.Star
    Icon.Start -> Icons.Default.Start
    Icon.Token -> Icons.Default.Token
    Icon.Toll -> Icons.Default.Toll
    Icon.Visibility -> Icons.Default.Visibility
    Icon.VolunteerActivism -> Icons.Default.VolunteerActivism
    Icon.WavingHand -> Icons.Default.WavingHand
    Icon.ArrowBack -> Icons.Default.ArrowBack
    Icon.Favorite -> Icons.Default.Favorite
    Icon.Search -> Icons.Default.Search
}
