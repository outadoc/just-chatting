package fr.outadoc.justchatting.feature.chat.presentation.mobile

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.jetbrains.compose.resources.stringResource
import fr.outadoc.justchatting.feature.chat.presentation.RoomState
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.utils.ui.format
import kotlin.time.Duration

@Composable
fun RoomStateBanner(
    modifier: Modifier = Modifier,
    roomState: RoomState,
) {
    SlimSnackbar(modifier = modifier) {
        with(roomState) {
            if (isEmoteOnly) {
                Text(text = stringResource(Res.string.room_emote))
            }

            if (!minFollowDuration.isNegative()) {
                Text(
                    text = when (minFollowDuration) {
                        Duration.ZERO -> stringResource(Res.string.room_followers)
                        else -> stringResource(
                            Res.string.room_followers_min,
                            minFollowDuration.format(LocalContext.current),
                        )
                    },
                )
            }

            if (uniqueMessagesOnly) {
                Text(text = stringResource(Res.string.room_unique))
            }

            if (slowModeDuration.isPositive()) {
                Text(
                    text = stringResource(
                        Res.string.room_slow,
                        slowModeDuration.format(LocalContext.current),
                    ),
                )
            }

            if (isSubOnly) {
                Text(text = stringResource(Res.string.room_subs))
            }
        }
    }
}
