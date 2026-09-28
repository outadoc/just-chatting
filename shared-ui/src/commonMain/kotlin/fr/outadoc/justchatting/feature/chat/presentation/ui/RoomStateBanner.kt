package fr.outadoc.justchatting.feature.chat.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fr.outadoc.justchatting.feature.chat.presentation.RoomMode
import fr.outadoc.justchatting.feature.chat.presentation.RoomState
import fr.outadoc.justchatting.feature.chat.presentation.activeModes
import fr.outadoc.justchatting.shared.internal.Res
import fr.outadoc.justchatting.shared.internal.room_emote
import fr.outadoc.justchatting.shared.internal.room_followers
import fr.outadoc.justchatting.shared.internal.room_followers_min
import fr.outadoc.justchatting.shared.internal.room_slow
import fr.outadoc.justchatting.shared.internal.room_subs
import fr.outadoc.justchatting.shared.internal.room_unique
import fr.outadoc.justchatting.utils.presentation.format
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

@Composable
internal fun RoomStateBanner(
    modifier: Modifier = Modifier,
    roomState: RoomState,
) {
    SlimSnackbar(modifier = modifier) {
        roomState.activeModes.forEach { mode ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector =
                        when (mode) {
                            RoomMode.EmoteOnly -> Icons.Filled.EmojiEmotions
                            is RoomMode.FollowersOnly -> Icons.Filled.Favorite
                            RoomMode.UniqueMessages -> Icons.Filled.Fingerprint
                            is RoomMode.Slow -> Icons.Filled.HourglassTop
                            RoomMode.SubscribersOnly -> Icons.Filled.Star
                        },
                    contentDescription = null,
                )

                Text(
                    text =
                        when (mode) {
                            RoomMode.EmoteOnly -> {
                                stringResource(Res.string.room_emote)
                            }

                            is RoomMode.FollowersOnly -> {
                                if (mode.minFollowDuration == Duration.ZERO) {
                                    stringResource(Res.string.room_followers)
                                } else {
                                    stringResource(Res.string.room_followers_min, mode.minFollowDuration.format())
                                }
                            }

                            RoomMode.UniqueMessages -> {
                                stringResource(Res.string.room_unique)
                            }

                            is RoomMode.Slow -> {
                                stringResource(Res.string.room_slow, mode.delay.format())
                            }

                            RoomMode.SubscribersOnly -> {
                                stringResource(Res.string.room_subs)
                            }
                        },
                )
            }
        }
    }
}
