package fr.outadoc.justchatting.component.twitch.websocket.irc

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Toll
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.outlined.Star
import dev.icerock.moko.resources.format
import fr.outadoc.justchatting.component.chatapi.common.ChatEvent
import fr.outadoc.justchatting.component.chatapi.common.Chatter
import fr.outadoc.justchatting.shared.MR
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.IrcEvent
import fr.outadoc.justchatting.utils.core.formatNumber
import fr.outadoc.justchatting.utils.ui.format
import kotlinx.collections.immutable.toImmutableList
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Currency

class IrcMessageMapper(private val context: Context) {

    fun mapMessage(ircEvent: IrcEvent.Message): ChatEvent = with(ircEvent) {
        when (this) {
            is IrcEvent.Message.Notice -> {
                ChatEvent.Message.Notice(
                    timestamp = timestamp,
                    text = getLabelForNotice(
                        messageId = messageId,
                        message = message,
                    ) ?: message,
                )
            }

            is IrcEvent.Message.IncomingRaid -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = Icons.Default.CallReceived,
                        subtitle = MR.strings.chat_raid_header
                            .format(
                                MR.plurals.viewers.format(
                                    number = raidersCount,
                                    raidersCount,
                                ),
                            )
                            .toString(context),
                    ),
                    body = null,
                )
            }

            is IrcEvent.Message.CancelledRaid -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = Icons.Default.Cancel,
                        subtitle = MR.strings.chat_unraid_subtitle.getString(context),
                    ),
                    body = null,
                )
            }

            is IrcEvent.Message.HighlightedMessage -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = MR.strings.irc_msgid_highlighted_message.getString(context),
                        titleIcon = Icons.Default.Highlight,
                        subtitle = null,
                    ),
                    body = userMessage.map(),
                )
            }

            is IrcEvent.Message.Announcement -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = MR.strings.irc_msgid_announcement.getString(context),
                        titleIcon = Icons.Default.Campaign,
                        subtitle = null,
                    ),
                    body = userMessage.map(),
                )
            }

            is IrcEvent.Message.Subscription -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = when (subscriptionPlan) {
                            SUB_PRIME -> Icons.Outlined.Star
                            else -> Icons.Filled.Star
                        },
                        subtitle = when (streakMonths) {
                            0 -> {
                                MR.strings.chat_sub_header_withDuration
                                    .format(
                                        parseSubscriptionTier(subscriptionPlan),
                                        MR.plurals.months.format(
                                            number = cumulativeMonths,
                                            cumulativeMonths.formatNumber(),
                                        ).toString(context),
                                    )
                                    .toString(context)
                            }

                            else -> {
                                MR.strings.chat_sub_header_withDurationAndStreak.format(
                                    parseSubscriptionTier(subscriptionPlan),
                                    MR.plurals.months.format(
                                        number = cumulativeMonths,
                                        cumulativeMonths.formatNumber(),
                                    ).toString(context),
                                    MR.plurals.months.format(
                                        number = streakMonths,
                                        streakMonths.formatNumber(),
                                    ).toString(context),
                                ).toString(context)
                            }
                        },
                    ),
                    body = userMessage?.map(),
                )
            }

            is IrcEvent.Message.SubscriptionConversion -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = Icons.Filled.Star,
                        subtitle = MR.strings.chat_subConversion_header
                            .format(parseSubscriptionTierWithArticle(subscriptionPlan))
                            .toString(context),
                    ),
                    body = userMessage?.map(),
                )
            }

            is IrcEvent.Message.MassSubscriptionGift -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = Icons.Default.VolunteerActivism,
                        subtitle = MR.strings.chat_massSubGift_header
                            .format(
                                giftCount.formatNumber(),
                                parseSubscriptionTierWithArticle(subscriptionPlan),
                                totalChannelGiftCount.formatNumber(),
                            )
                            .toString(context),
                    ),
                    body = null,
                )
            }

            is IrcEvent.Message.SubscriptionGift -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    body = null,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = Icons.Default.Redeem,
                        subtitle = MR.strings.chat_subGift_header
                            .format(
                                parseSubscriptionTier(subscriptionPlan),
                                recipientDisplayName,
                                MR.plurals.months.format(
                                    number = cumulativeMonths,
                                    cumulativeMonths.formatNumber(),
                                ).toString(context),
                            )
                            .toString(context),
                    ),
                )
            }

            is IrcEvent.Message.GiftPayForward -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = userDisplayName,
                        titleIcon = Icons.Default.FastForward,
                        subtitle = when (priorGifterDisplayName) {
                            null -> MR.strings.chat_subGift_payForwardAnonymous.getString(context)
                            else -> {
                                MR.strings.chat_subGift_payForward
                                    .format(priorGifterDisplayName)
                                    .toString(context)
                            }
                        },
                    ),
                    body = null,
                )
            }

            is IrcEvent.Message.UserNotice -> {
                ChatEvent.Message.Highlighted(
                    timestamp = timestamp,
                    body = userMessage?.map(),
                    metadata = ChatEvent.Message.Highlighted.Metadata(
                        title = systemMsg,
                        titleIcon = null,
                        subtitle = null,
                    ),
                )
            }

            is IrcEvent.Message.ChatMessage -> {
                val metadata = when {
                    isFirstMessageByUser -> {
                        ChatEvent.Message.Highlighted.Metadata(
                            title = MR.strings.chat_first.getString(context),
                            titleIcon = Icons.Default.WavingHand,
                            subtitle = null,
                        )
                    }

                    rewardId != null -> {
                        ChatEvent.Message.Highlighted.Metadata(
                            title = MR.strings.chat_reward.getString(context),
                            titleIcon = Icons.Default.Toll,
                            subtitle = null,
                        )
                    }

                    paidMessageInfo != null -> {
                        val currency = Currency.getInstance(paidMessageInfo.currency)
                        val amount = BigDecimal.valueOf(
                            paidMessageInfo.amount,
                            paidMessageInfo.exponent,
                        )

                        val formattedAmount =
                            NumberFormat.getCurrencyInstance()
                                .apply { this.currency = currency }
                                .format(amount)

                        val header: String =
                            MR.strings.chat_paidMessage
                                .format(formattedAmount)
                                .toString(context)

                        ChatEvent.Message.Highlighted.Metadata(
                            title = header,
                            titleIcon = Icons.Default.Bolt,
                            subtitle = null,
                            level = when (paidMessageInfo.level) {
                                "ONE" -> ChatEvent.Message.Highlighted.Level.One
                                "TWO" -> ChatEvent.Message.Highlighted.Level.Two
                                "THREE" -> ChatEvent.Message.Highlighted.Level.Three
                                "FOUR" -> ChatEvent.Message.Highlighted.Level.Four
                                "FIVE" -> ChatEvent.Message.Highlighted.Level.Five
                                "SIX" -> ChatEvent.Message.Highlighted.Level.Six
                                "SEVEN" -> ChatEvent.Message.Highlighted.Level.Seven
                                "EIGHT" -> ChatEvent.Message.Highlighted.Level.Eight
                                "NINE" -> ChatEvent.Message.Highlighted.Level.Nine
                                "TEN" -> ChatEvent.Message.Highlighted.Level.Ten
                                else -> ChatEvent.Message.Highlighted.Level.Base
                            },
                        )
                    }

                    else -> {
                        null
                    }
                }

                if (metadata != null) {
                    ChatEvent.Message.Highlighted(
                        timestamp = timestamp,
                        body = map(),
                        metadata = metadata,
                    )
                } else {
                    ChatEvent.Message.Simple(
                        body = map(),
                        timestamp = timestamp,
                    )
                }
            }
        }
    }

    private fun parseSubscriptionTier(planId: String): String {
        return when (planId) {
            SUB_T1 -> MR.strings.chat_sub_tier1.getString(context)
            SUB_T2 -> MR.strings.chat_sub_tier2.getString(context)
            SUB_T3 -> MR.strings.chat_sub_tier3.getString(context)
            SUB_PRIME -> MR.strings.chat_sub_prime.getString(context)
            else -> planId
        }
    }

    private fun parseSubscriptionTierWithArticle(planId: String): String {
        return when (planId) {
            SUB_T1 -> MR.strings.chat_subGift_tier1.getString(context)
            SUB_T2 -> MR.strings.chat_subGift_tier2.getString(context)
            SUB_T3 -> MR.strings.chat_subGift_tier3.getString(context)
            else -> planId
        }
    }

    private fun IrcEvent.Message.ChatMessage.map(): ChatEvent.Message.Body {
        return ChatEvent.Message.Body(
            message = message.orEmpty(),
            messageId = id,
            chatter = Chatter(
                id = userId,
                displayName = userName,
                login = userLogin,
            ),
            isAction = isAction,
            color = color,
            embeddedEmotes = embeddedEmotes.orEmpty().toImmutableList(),
            badges = badges.orEmpty().toImmutableList(),
            inReplyTo = inReplyTo?.let {
                ChatEvent.Message.Body.InReplyTo(
                    id = inReplyTo.id,
                    message = inReplyTo.message,
                    chatter = Chatter(
                        id = inReplyTo.id,
                        login = inReplyTo.userLogin,
                        displayName = inReplyTo.userName,
                    ),
                )
            },
        )
    }

    fun mapOptional(command: IrcEvent): ChatEvent? {
        return when (command) {
            is IrcEvent.Command.ClearChat -> {
                if (command.targetUserLogin != null) {
                    if (command.duration == null) {
                        ChatEvent.Message.Highlighted(
                            timestamp = command.timestamp,
                            metadata = ChatEvent.Message.Highlighted.Metadata(
                                title = command.targetUserLogin,
                                titleIcon = Icons.Default.Gavel,
                                subtitle = MR.strings.chat_ban.getString(context),
                            ),
                            body = null,
                        )
                    } else {
                        ChatEvent.Message.Highlighted(
                            timestamp = command.timestamp,
                            metadata = ChatEvent.Message.Highlighted.Metadata(
                                title = command.targetUserLogin,
                                titleIcon = Icons.Default.Gavel,
                                subtitle = MR.strings.chat_timeout
                                    .format(command.duration.format(context))
                                    .toString(context),
                            ),
                            body = null,
                        )
                    }
                } else {
                    ChatEvent.Message.Notice(
                        timestamp = command.timestamp,
                        text = MR.strings.chat_clear.getString(context),
                    )
                }
            }

            else -> null
        }
    }

    private fun getLabelForNotice(messageId: String?, message: String?): String? {
        return when (messageId) {
            "already_banned" -> {
                MR.strings.irc_notice_already_banned
                    .format(
                        message?.substringBefore(" is already banned", "") ?: "",
                    )
                    .toString(context)
            }

            "already_emote_only_off" -> {
                MR.strings.irc_notice_already_emote_only_off.getString(context)
            }

            "already_emote_only_on" -> {
                MR.strings.irc_notice_already_emote_only_on.getString(context)
            }

            "already_followers_off" -> {
                MR.strings.irc_notice_already_followers_off.getString(context)
            }

            "already_followers_on" -> {
                MR.strings.irc_notice_already_followers_on.format(
                    message?.substringAfter("is already in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                ).toString(context)
            }

            "already_r9k_off" -> {
                MR.strings.irc_notice_already_r9k_off.getString(context)
            }

            "already_r9k_on" -> {
                MR.strings.irc_notice_already_r9k_on.getString(context)
            }

            "already_slow_off" -> {
                MR.strings.irc_notice_already_slow_off.getString(context)
            }

            "already_slow_on" -> {
                MR.strings.irc_notice_already_slow_on.format(
                    message?.substringAfter("is already in ", "")
                        ?.substringBefore("-second slow", "")
                        ?: "",
                ).toString(context)
            }

            "already_subs_off" -> {
                MR.strings.irc_notice_already_subs_off.getString(context)
            }

            "already_subs_on" -> {
                MR.strings.irc_notice_already_subs_on.getString(context)
            }

            "autohost_receive" -> {
                MR.strings.irc_notice_autohost_receive.format(
                    message?.substringBefore(" is now auto hosting", "") ?: "",
                    message?.substringAfter("you for up to ", "")?.substringBefore(" viewers", "")
                        ?: "",
                ).toString(context)
            }

            "bad_ban_admin" -> {
                MR.strings.irc_notice_bad_ban_admin.format(
                    message?.substringAfter("cannot ban admin", "")
                        ?.substringBefore(". Please email", "") ?: "",
                ).toString(context)
            }

            "bad_ban_anon" -> {
                MR.strings.irc_notice_bad_ban_anon.getString(context)
            }

            "bad_ban_broadcaster" -> {
                MR.strings.irc_notice_bad_ban_broadcaster.getString(context)
            }

            "bad_ban_mod" -> {
                MR.strings.irc_notice_bad_ban_mod.format(
                    message?.substringAfter("cannot ban moderator", "")
                        ?.substringBefore(" unless you are", "") ?: "",
                ).toString(context)
            }

            "bad_ban_self" -> {
                MR.strings.irc_notice_bad_ban_self.getString(context)
            }

            "bad_ban_staff" -> {
                MR.strings.irc_notice_bad_ban_staff.format(
                    message?.substringAfter("cannot ban staff", "")
                        ?.substringBefore(". Please email", "") ?: "",
                ).toString(context)
            }

            "bad_commercial_error" -> {
                MR.strings.irc_notice_bad_commercial_error.getString(context)
            }

            "bad_delete_message_broadcaster" -> {
                MR.strings.irc_notice_bad_delete_message_broadcaster.getString(context)
            }

            "bad_delete_message_mod" -> {
                MR.strings.irc_notice_bad_delete_message_mod.format(
                    message?.substringAfter("from another moderator ", "")
                        ?.substringBeforeLast(".", "")
                        ?: "",
                ).toString(context)
            }

            "bad_host_error" -> {
                MR.strings.irc_notice_bad_host_error.format(
                    message?.substringAfter("a problem hosting ", "")
                        ?.substringBefore(". Please try", "") ?: "",
                ).toString(context)
            }

            "bad_host_hosting" -> {
                MR.strings.irc_notice_bad_host_hosting.format(
                    message?.substringAfter("is already hosting ", "")?.substringBeforeLast(".", "")
                        ?: "",
                ).toString(context)
            }

            "bad_host_rate_exceeded" -> {
                MR.strings.irc_notice_bad_host_rate_exceeded.format(
                    message?.substringAfter("changed more than ", "")
                        ?.substringBefore(" times every half", "") ?: "",
                ).toString(context)
            }

            "bad_host_rejected" -> {
                MR.strings.irc_notice_bad_host_rejected.getString(context)
            }

            "bad_host_self" -> {
                MR.strings.irc_notice_bad_host_self.getString(context)
            }

            "bad_mod_banned" -> {
                MR.strings.irc_notice_bad_mod_banned.format(
                    message?.substringBefore(" is banned", "") ?: "",
                ).toString(context)
            }

            "bad_mod_mod" -> {
                MR.strings.irc_notice_bad_mod_mod.format(
                    message?.substringBefore(" is already", "") ?: "",
                ).toString(context)
            }

            "bad_slow_duration" -> {
                MR.strings.irc_notice_bad_slow_duration.format(
                    message?.substringAfter("to more than ", "")?.substringBefore(" seconds.", "")
                        ?: "",
                ).toString(context)
            }

            "bad_timeout_admin" -> {
                MR.strings.irc_notice_bad_timeout_admin.format(
                    message?.substringAfter("cannot timeout admin ", "")
                        ?.substringBefore(". Please email", "") ?: "",
                ).toString(context)
            }

            "bad_timeout_anon" -> {
                MR.strings.irc_notice_bad_timeout_anon.getString(context)
            }

            "bad_timeout_broadcaster" -> {
                MR.strings.irc_notice_bad_timeout_broadcaster.getString(context)
            }

            "bad_timeout_duration" -> {
                MR.strings.irc_notice_bad_timeout_duration.format(
                    message?.substringAfter("for more than ", "")?.substringBeforeLast(".", "")
                        ?: "",
                ).toString(context)
            }

            "bad_timeout_mod" -> {
                MR.strings.irc_notice_bad_timeout_mod.format(
                    message?.substringAfter("cannot timeout moderator ", "")
                        ?.substringBefore(" unless you are", "") ?: "",
                ).toString(context)
            }

            "bad_timeout_self" -> {
                MR.strings.irc_notice_bad_timeout_self.getString(context)
            }

            "bad_timeout_staff" -> {
                MR.strings.irc_notice_bad_timeout_staff.format(
                    message?.substringAfter("cannot timeout staff ", "")
                        ?.substringBefore(". Please email", "") ?: "",
                ).toString(context)
            }

            "bad_unban_no_ban" -> {
                MR.strings.irc_notice_bad_unban_no_ban.format(
                    message?.substringBefore(" is not banned", "") ?: "",
                ).toString(context)
            }

            "bad_unhost_error" -> {
                MR.strings.irc_notice_bad_unhost_error.getString(context)
            }

            "bad_unmod_mod" -> {
                MR.strings.irc_notice_bad_unmod_mod.format(
                    message?.substringBefore(" is not a", "") ?: "",
                ).toString(context)
            }

            "bad_vip_grantee_banned" -> {
                MR.strings.irc_notice_bad_vip_grantee_banned.format(
                    message?.substringBefore(" is banned in", "") ?: "",
                ).toString(context)
            }

            "bad_vip_grantee_already_vip" -> {
                MR.strings.irc_notice_bad_vip_grantee_already_vip.format(
                    message?.substringBefore(" is already a", "") ?: "",
                ).toString(context)
            }

            "bad_vip_max_vips_reached" -> {
                MR.strings.irc_notice_bad_vip_max_vips_reached.getString(context)
            }

            "bad_vip_achievement_incomplete" -> {
                MR.strings.irc_notice_bad_vip_achievement_incomplete.getString(context)
            }

            "bad_unvip_grantee_not_vip" -> {
                MR.strings.irc_notice_bad_unvip_grantee_not_vip.format(
                    message?.substringBefore(" is not a", "") ?: "",
                ).toString(context)
            }

            "ban_success" -> {
                MR.strings.irc_notice_ban_success.format(
                    message?.substringBefore(" is now banned", "") ?: "",
                ).toString(context)
            }

            "cmds_available" -> {
                MR.strings.irc_notice_cmds_available.format(
                    message?.substringAfter("details): ", "")?.substringBefore(" More help:", "")
                        ?: "",
                ).toString(context)
            }

            "color_changed" -> {
                MR.strings.irc_notice_color_changed.getString(context)
            }

            "commercial_success" -> {
                MR.strings.irc_notice_commercial_success.format(
                    message?.substringAfter("Initiating ", "")
                        ?.substringBefore(" second commercial break.", "") ?: "",
                ).toString(context)
            }

            "delete_message_success" -> {
                MR.strings.irc_notice_delete_message_success.format(
                    message?.substringAfter("The message from ", "")
                        ?.substringBefore(" is now deleted.", "") ?: "",
                ).toString(context)
            }

            "delete_staff_message_success" -> {
                MR.strings.irc_notice_delete_staff_message_success.format(
                    message?.substringAfter("message from staff ", "")
                        ?.substringBefore(". Please email", "") ?: "",
                ).toString(context)
            }

            "emote_only_off" -> {
                MR.strings.irc_notice_emote_only_off.getString(context)
            }

            "emote_only_on" -> {
                MR.strings.irc_notice_emote_only_on.getString(context)
            }

            "followers_off" -> {
                MR.strings.irc_notice_followers_off.getString(context)
            }

            "followers_on" -> {
                MR.strings.irc_notice_followers_on.format(
                    message?.substringAfter("is now in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                ).toString(context)
            }

            "followers_on_zero" -> {
                MR.strings.irc_notice_followers_on_zero.getString(context)
            }

            "host_off" -> {
                MR.strings.irc_notice_host_off.getString(context)
            }

            "host_on" -> {
                MR.strings.irc_notice_host_on.format(
                    message?.substringAfter("Now hosting ", "")?.substringBeforeLast(".", "") ?: "",
                ).toString(context)
            }

            "host_receive" -> {
                MR.strings.irc_notice_host_receive.format(
                    message?.substringBefore(" is now hosting", "") ?: "",
                    message?.substringAfter("you for up to ", "")?.substringBefore(" viewers", "")
                        ?: "",
                ).toString(context)
            }

            "host_receive_no_count" -> {
                MR.strings.irc_notice_host_receive_no_count.format(
                    message?.substringBefore(" is now hosting", "") ?: "",
                ).toString(context)
            }

            "host_target_went_offline" -> {
                MR.strings.irc_notice_host_target_went_offline.format(
                    message?.substringBefore(" has gone offline", "") ?: "",
                ).toString(context)
            }

            "hosts_remaining" -> {
                MR.strings.irc_notice_hosts_remaining.format(
                    message?.substringBefore(" host commands", "") ?: "",
                ).toString(context)
            }

            "invalid_user" -> {
                MR.strings.irc_notice_invalid_user.format(
                    message?.substringAfter("Invalid username: ", "") ?: "",
                ).toString(context)
            }

            "mod_success" -> {
                MR.strings.irc_notice_mod_success.format(
                    message?.substringAfter("You have added ", "")
                        ?.substringBefore(" as a moderator", "") ?: "",
                ).toString(context)
            }

            "msg_banned" -> {
                MR.strings.irc_notice_msg_banned.format(
                    message?.substringAfter("from talking in ", "")?.substringBeforeLast(".", "")
                        ?: "",
                ).toString(context)
            }

            "msg_bad_characters" -> {
                MR.strings.irc_notice_msg_bad_characters.getString(context)
            }

            "msg_channel_blocked" -> {
                MR.strings.irc_notice_msg_channel_blocked.getString(context)
            }

            "msg_channel_suspended" -> {
                MR.strings.irc_notice_msg_channel_suspended.getString(context)
            }

            "msg_duplicate" -> {
                MR.strings.irc_notice_msg_duplicate.getString(context)
            }

            "msg_emoteonly" -> {
                MR.strings.irc_notice_msg_emoteonly.getString(context)
            }

            "msg_followersonly" -> {
                MR.strings.irc_notice_msg_followersonly.format(
                    message?.substringAfter("This room is in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                    message?.substringAfter("Follow ", "")?.substringBefore(" to join", "") ?: "",
                ).toString(context)
            }

            "msg_followersonly_followed" -> {
                MR.strings.irc_notice_msg_followersonly_followed.format(
                    message?.substringAfter("This room is in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                    message?.substringAfter("following for ", "")?.substringBefore(". Continue", "")
                        ?: "",
                ).toString(context)
            }

            "msg_followersonly_zero" -> {
                MR.strings.irc_notice_msg_followersonly_zero.format(
                    message?.substringAfter(". Follow ", "")?.substringBefore(" to join the", "")
                        ?: "",
                ).toString(context)
            }

            "msg_r9k" -> {
                MR.strings.irc_notice_msg_r9k.getString(context)
            }

            "msg_ratelimit" -> {
                MR.strings.irc_notice_msg_ratelimit.getString(context)
            }

            "msg_rejected" -> {
                MR.strings.irc_notice_msg_rejected.getString(context)
            }

            "msg_rejected_mandatory" -> {
                MR.strings.irc_notice_msg_rejected_mandatory.getString(context)
            }

            "msg_slowmode" -> {
                MR.strings.irc_notice_msg_slowmode.format(
                    message?.substringAfter("talk again in ", "")?.substringBefore(" seconds.", "")
                        ?: "",
                ).toString(context)
            }

            "msg_subsonly" -> {
                MR.strings.irc_notice_msg_subsonly.format(
                    message?.substringAfter("/products/", "")?.substringBefore("/ticket?ref", "")
                        ?: "",
                ).toString(context)
            }

            "msg_suspended" -> {
                MR.strings.irc_notice_msg_suspended.getString(context)
            }

            "msg_timedout" -> {
                MR.strings.irc_notice_msg_timedout.format(
                    message?.substringAfter("timed out for ", "")
                        ?.substringBefore(" more seconds.", "")
                        ?: "",
                ).toString(context)
            }

            "msg_verified_email" -> {
                MR.strings.irc_notice_msg_verified_email.getString(context)
            }

            "no_help" -> {
                MR.strings.irc_notice_no_help.getString(context)
            }

            "no_mods" -> {
                MR.strings.irc_notice_no_mods.getString(context)
            }

            "no_vips" -> {
                MR.strings.irc_notice_no_vips.getString(context)
            }

            "not_hosting" -> {
                MR.strings.irc_notice_not_hosting.getString(context)
            }

            "no_permission" -> {
                MR.strings.irc_notice_no_permission.getString(context)
            }

            "r9k_off" -> {
                MR.strings.irc_notice_r9k_off.getString(context)
            }

            "r9k_on" -> {
                MR.strings.irc_notice_r9k_on.getString(context)
            }

            "raid_error_already_raiding" -> {
                MR.strings.irc_notice_raid_error_already_raiding.getString(context)
            }

            "raid_error_forbidden" -> {
                MR.strings.irc_notice_raid_error_forbidden.getString(context)
            }

            "raid_error_self" -> {
                MR.strings.irc_notice_raid_error_self.getString(context)
            }

            "raid_error_too_many_viewers" -> {
                MR.strings.irc_notice_raid_error_too_many_viewers.getString(context)
            }

            "raid_error_unexpected" -> {
                MR.strings.irc_notice_raid_error_unexpected.format(
                    message?.substringAfter("a problem raiding ", "")
                        ?.substringBefore(". Please try", "") ?: "",
                ).toString(context)
            }

            "raid_notice_mature" -> {
                MR.strings.irc_notice_raid_notice_mature.getString(context)
            }

            "raid_notice_restricted_chat" -> {
                MR.strings.irc_notice_raid_notice_restricted_chat.getString(context)
            }

            "room_mods" -> {
                MR.strings.irc_notice_room_mods.format(
                    message?.substringAfter("this channel are: ", "") ?: "",
                ).toString(context)
            }

            "slow_off" -> {
                MR.strings.irc_notice_slow_off.getString(context)
            }

            "slow_on" -> {
                MR.strings.irc_notice_slow_on.format(
                    message?.substringAfter("send messages every ", "")
                        ?.substringBefore(" seconds.", "") ?: "",
                ).toString(context)
            }

            "subs_off" -> {
                MR.strings.irc_notice_subs_off.getString(context)
            }

            "subs_on" -> {
                MR.strings.irc_notice_subs_on.getString(context)
            }

            "timeout_no_timeout" -> {
                MR.strings.irc_notice_timeout_no_timeout.format(
                    message?.substringBefore(" is not timed", "") ?: "",
                ).toString(context)
            }

            "timeout_success" -> {
                MR.strings.irc_notice_timeout_success.format(
                    message?.substringBefore(" has been", "") ?: "",
                    message?.substringAfter("timed out for ", "")?.substringBeforeLast(".", "")
                        ?: "",
                ).toString(context)
            }

            "tos_ban" -> {
                MR.strings.irc_notice_tos_ban.format(
                    message?.substringAfter("has closed channel ", "")
                        ?.substringBefore(" due to Terms", "") ?: "",
                ).toString(context)
            }

            "turbo_only_color" -> {
                MR.strings.irc_notice_turbo_only_color.format(
                    message?.substringAfter("following instead: ", "") ?: "",
                ).toString(context)
            }

            "unavailable_command" -> {
                MR.strings.irc_notice_unavailable_command.format(
                    message?.substringAfter("Sorry, “", "")
                        ?.substringBefore("” is not available", "")
                        ?: "",
                ).toString(context)
            }

            "unban_success" -> {
                MR.strings.irc_notice_unban_success.format(
                    message?.substringBefore(" is no longer", "") ?: "",
                ).toString(context)
            }

            "unmod_success" -> {
                MR.strings.irc_notice_unmod_success.format(
                    message?.substringAfter("You have removed ", "")
                        ?.substringBefore(" as a moderator", "") ?: "",
                ).toString(context)
            }

            "unraid_error_no_active_raid" -> {
                MR.strings.irc_notice_unraid_error_no_active_raid.getString(context)
            }

            "unraid_error_unexpected" -> {
                MR.strings.irc_notice_unraid_error_unexpected.getString(context)
            }

            "unraid_success" -> {
                MR.strings.irc_notice_unraid_success.getString(context)
            }

            "unrecognized_cmd" -> {
                MR.strings.irc_notice_unrecognized_cmd.format(
                    message?.substringAfter("Unrecognized command: ", "") ?: "",
                ).toString(context)
            }

            "untimeout_banned" -> {
                MR.strings.irc_notice_untimeout_banned.format(
                    message?.substringBefore(" is permanently banned", "") ?: "",
                ).toString(context)
            }

            "untimeout_success" -> {
                MR.strings.irc_notice_untimeout_success.format(
                    message?.substringBefore(" is no longer", "") ?: "",
                ).toString(context)
            }

            "unvip_success" -> {
                MR.strings.irc_notice_unvip_success.format(
                    message?.substringAfter("You have removed ", "")
                        ?.substringBefore(" as a VIP", "")
                        ?: "",
                ).toString(context)
            }

            "usage_ban" -> {
                MR.strings.irc_notice_usage_ban.getString(context)
            }

            "usage_clear" -> {
                MR.strings.irc_notice_usage_clear.getString(context)
            }

            "usage_color" -> {
                MR.strings.irc_notice_usage_color.format(
                    message?.substringAfter("following: ", "")?.substringBeforeLast(".", "") ?: "",
                ).toString(context)
            }

            "usage_commercial" -> {
                MR.strings.irc_notice_usage_commercial.getString(context)
            }

            "usage_disconnect" -> {
                MR.strings.irc_notice_usage_disconnect.getString(context)
            }

            "usage_delete" -> {
                MR.strings.irc_notice_usage_delete.getString(context)
            }

            "usage_emote_only_off" -> {
                MR.strings.irc_notice_usage_emote_only_off.getString(context)
            }

            "usage_emote_only_on" -> {
                MR.strings.irc_notice_usage_emote_only_on.getString(context)
            }

            "usage_followers_off" -> {
                MR.strings.irc_notice_usage_followers_off.getString(context)
            }

            "usage_followers_on" -> {
                MR.strings.irc_notice_usage_followers_on.getString(context)
            }

            "usage_help" -> {
                MR.strings.irc_notice_usage_help.getString(context)
            }

            "usage_host" -> {
                MR.strings.irc_notice_usage_host.getString(context)
            }

            "usage_marker" -> {
                MR.strings.irc_notice_usage_marker.getString(context)
            }

            "usage_me" -> {
                MR.strings.irc_notice_usage_me.getString(context)
            }

            "usage_mod" -> {
                MR.strings.irc_notice_usage_mod.getString(context)
            }

            "usage_mods" -> {
                MR.strings.irc_notice_usage_mods.getString(context)
            }

            "usage_r9k_off" -> {
                MR.strings.irc_notice_usage_r9k_off.getString(context)
            }

            "usage_r9k_on" -> {
                MR.strings.irc_notice_usage_r9k_on.getString(context)
            }

            "usage_raid" -> {
                MR.strings.irc_notice_usage_raid.getString(context)
            }

            "usage_slow_off" -> {
                MR.strings.irc_notice_usage_slow_off.getString(context)
            }

            "usage_slow_on" -> {
                MR.strings.irc_notice_usage_slow_on.format(
                    message?.substringAfter("default=", "")?.substringBefore(")", "") ?: "",
                ).toString(context)
            }

            "usage_subs_off" -> {
                MR.strings.irc_notice_usage_subs_off.getString(context)
            }

            "usage_subs_on" -> {
                MR.strings.irc_notice_usage_subs_on.getString(context)
            }

            "usage_timeout" -> {
                MR.strings.irc_notice_usage_timeout.getString(context)
            }

            "usage_unban" -> {
                MR.strings.irc_notice_usage_unban.getString(context)
            }

            "usage_unhost" -> {
                MR.strings.irc_notice_usage_unhost.getString(context)
            }

            "usage_unmod" -> {
                MR.strings.irc_notice_usage_unmod.getString(context)
            }

            "usage_unraid" -> {
                MR.strings.irc_notice_usage_unraid.getString(context)
            }

            "usage_untimeout" -> {
                MR.strings.irc_notice_usage_untimeout.getString(context)
            }

            "usage_unvip" -> {
                MR.strings.irc_notice_usage_unvip.getString(context)
            }

            "usage_user" -> {
                MR.strings.irc_notice_usage_user.getString(context)
            }

            "usage_vip" -> {
                MR.strings.irc_notice_usage_vip.getString(context)
            }

            "usage_vips" -> {
                MR.strings.irc_notice_usage_vips.getString(context)
            }

            "usage_whisper" -> {
                MR.strings.irc_notice_usage_whisper.getString(context)
            }

            "vip_success" -> {
                MR.strings.irc_notice_vip_success.format(
                    message?.substringAfter("You have added ", "")
                        ?.substringBeforeLast(" as a vip", "")
                        ?: "",
                ).toString(context)
            }

            "vips_success" -> {
                MR.strings.irc_notice_vips_success.format(
                    message?.substringAfter("channel are: ", "")?.substringBeforeLast(".", "")
                        ?: "",
                ).toString(context)
            }

            "whisper_banned" -> {
                MR.strings.irc_notice_whisper_banned.getString(context)
            }

            "whisper_banned_recipient" -> {
                MR.strings.irc_notice_whisper_banned_recipient.getString(context)
            }

            "whisper_invalid_login" -> {
                MR.strings.irc_notice_whisper_invalid_login.getString(context)
            }

            "whisper_invalid_self" -> {
                MR.strings.irc_notice_whisper_invalid_self.getString(context)
            }

            "whisper_limit_per_min" -> {
                MR.strings.irc_notice_whisper_limit_per_min.getString(context)
            }

            "whisper_limit_per_sec" -> {
                MR.strings.irc_notice_whisper_limit_per_sec.getString(context)
            }

            "whisper_restricted" -> {
                MR.strings.irc_notice_whisper_restricted.getString(context)
            }

            "whisper_restricted_recipient" -> {
                MR.strings.irc_notice_whisper_restricted_recipient.getString(context)
            }

            else -> {
                null
            }
        }
    }

    private companion object {
        const val SUB_T1 = "1000"
        const val SUB_T2 = "2000"
        const val SUB_T3 = "3000"
        const val SUB_PRIME = "Prime"
    }
}
