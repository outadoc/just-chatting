package fr.outadoc.justchatting.feature.chat.presentation

import fr.outadoc.justchatting.feature.chat.domain.model.ChatEvent
import fr.outadoc.justchatting.feature.chat.domain.model.ChatListItem
import fr.outadoc.justchatting.feature.chat.domain.model.Chatter
import fr.outadoc.justchatting.feature.chat.domain.model.Icon
import fr.outadoc.justchatting.feature.timeline.domain.model.StreamCategory
import fr.outadoc.justchatting.shared.Res
import fr.outadoc.justchatting.shared.chat_ban
import fr.outadoc.justchatting.shared.chat_clear
import fr.outadoc.justchatting.shared.chat_first
import fr.outadoc.justchatting.shared.chat_join
import fr.outadoc.justchatting.shared.chat_massSubGift_header
import fr.outadoc.justchatting.shared.chat_raid_header
import fr.outadoc.justchatting.shared.chat_reward
import fr.outadoc.justchatting.shared.chat_send_msg_error
import fr.outadoc.justchatting.shared.chat_subConversion_header
import fr.outadoc.justchatting.shared.chat_subGift_header
import fr.outadoc.justchatting.shared.chat_subGift_payForward
import fr.outadoc.justchatting.shared.chat_subGift_payForwardAnonymous
import fr.outadoc.justchatting.shared.chat_subGift_tier1
import fr.outadoc.justchatting.shared.chat_subGift_tier2
import fr.outadoc.justchatting.shared.chat_subGift_tier3
import fr.outadoc.justchatting.shared.chat_sub_header_withDuration
import fr.outadoc.justchatting.shared.chat_sub_header_withDurationAndStreak
import fr.outadoc.justchatting.shared.chat_sub_prime
import fr.outadoc.justchatting.shared.chat_sub_tier1
import fr.outadoc.justchatting.shared.chat_sub_tier2
import fr.outadoc.justchatting.shared.chat_sub_tier3
import fr.outadoc.justchatting.shared.chat_timeout
import fr.outadoc.justchatting.shared.chat_unraid_subtitle
import fr.outadoc.justchatting.shared.irc_msgid_announcement
import fr.outadoc.justchatting.shared.irc_msgid_highlighted_message
import fr.outadoc.justchatting.shared.irc_notice_already_banned
import fr.outadoc.justchatting.shared.irc_notice_already_emote_only_off
import fr.outadoc.justchatting.shared.irc_notice_already_emote_only_on
import fr.outadoc.justchatting.shared.irc_notice_already_followers_off
import fr.outadoc.justchatting.shared.irc_notice_already_followers_on
import fr.outadoc.justchatting.shared.irc_notice_already_r9k_off
import fr.outadoc.justchatting.shared.irc_notice_already_r9k_on
import fr.outadoc.justchatting.shared.irc_notice_already_slow_off
import fr.outadoc.justchatting.shared.irc_notice_already_slow_on
import fr.outadoc.justchatting.shared.irc_notice_already_subs_off
import fr.outadoc.justchatting.shared.irc_notice_already_subs_on
import fr.outadoc.justchatting.shared.irc_notice_autohost_receive
import fr.outadoc.justchatting.shared.irc_notice_bad_ban_admin
import fr.outadoc.justchatting.shared.irc_notice_bad_ban_anon
import fr.outadoc.justchatting.shared.irc_notice_bad_ban_broadcaster
import fr.outadoc.justchatting.shared.irc_notice_bad_ban_mod
import fr.outadoc.justchatting.shared.irc_notice_bad_ban_self
import fr.outadoc.justchatting.shared.irc_notice_bad_ban_staff
import fr.outadoc.justchatting.shared.irc_notice_bad_commercial_error
import fr.outadoc.justchatting.shared.irc_notice_bad_delete_message_broadcaster
import fr.outadoc.justchatting.shared.irc_notice_bad_delete_message_mod
import fr.outadoc.justchatting.shared.irc_notice_bad_host_error
import fr.outadoc.justchatting.shared.irc_notice_bad_host_hosting
import fr.outadoc.justchatting.shared.irc_notice_bad_host_rate_exceeded
import fr.outadoc.justchatting.shared.irc_notice_bad_host_rejected
import fr.outadoc.justchatting.shared.irc_notice_bad_host_self
import fr.outadoc.justchatting.shared.irc_notice_bad_mod_banned
import fr.outadoc.justchatting.shared.irc_notice_bad_mod_mod
import fr.outadoc.justchatting.shared.irc_notice_bad_slow_duration
import fr.outadoc.justchatting.shared.irc_notice_bad_timeout_admin
import fr.outadoc.justchatting.shared.irc_notice_bad_timeout_anon
import fr.outadoc.justchatting.shared.irc_notice_bad_timeout_broadcaster
import fr.outadoc.justchatting.shared.irc_notice_bad_timeout_duration
import fr.outadoc.justchatting.shared.irc_notice_bad_timeout_mod
import fr.outadoc.justchatting.shared.irc_notice_bad_timeout_self
import fr.outadoc.justchatting.shared.irc_notice_bad_timeout_staff
import fr.outadoc.justchatting.shared.irc_notice_bad_unban_no_ban
import fr.outadoc.justchatting.shared.irc_notice_bad_unhost_error
import fr.outadoc.justchatting.shared.irc_notice_bad_unmod_mod
import fr.outadoc.justchatting.shared.irc_notice_bad_unvip_grantee_not_vip
import fr.outadoc.justchatting.shared.irc_notice_bad_vip_achievement_incomplete
import fr.outadoc.justchatting.shared.irc_notice_bad_vip_grantee_already_vip
import fr.outadoc.justchatting.shared.irc_notice_bad_vip_grantee_banned
import fr.outadoc.justchatting.shared.irc_notice_bad_vip_max_vips_reached
import fr.outadoc.justchatting.shared.irc_notice_ban_success
import fr.outadoc.justchatting.shared.irc_notice_cmds_available
import fr.outadoc.justchatting.shared.irc_notice_color_changed
import fr.outadoc.justchatting.shared.irc_notice_commercial_success
import fr.outadoc.justchatting.shared.irc_notice_delete_message_success
import fr.outadoc.justchatting.shared.irc_notice_delete_staff_message_success
import fr.outadoc.justchatting.shared.irc_notice_emote_only_off
import fr.outadoc.justchatting.shared.irc_notice_emote_only_on
import fr.outadoc.justchatting.shared.irc_notice_followers_off
import fr.outadoc.justchatting.shared.irc_notice_followers_on
import fr.outadoc.justchatting.shared.irc_notice_followers_on_zero
import fr.outadoc.justchatting.shared.irc_notice_host_off
import fr.outadoc.justchatting.shared.irc_notice_host_on
import fr.outadoc.justchatting.shared.irc_notice_host_receive
import fr.outadoc.justchatting.shared.irc_notice_host_receive_no_count
import fr.outadoc.justchatting.shared.irc_notice_host_target_went_offline
import fr.outadoc.justchatting.shared.irc_notice_hosts_remaining
import fr.outadoc.justchatting.shared.irc_notice_invalid_user
import fr.outadoc.justchatting.shared.irc_notice_mod_success
import fr.outadoc.justchatting.shared.irc_notice_msg_bad_characters
import fr.outadoc.justchatting.shared.irc_notice_msg_banned
import fr.outadoc.justchatting.shared.irc_notice_msg_channel_blocked
import fr.outadoc.justchatting.shared.irc_notice_msg_channel_suspended
import fr.outadoc.justchatting.shared.irc_notice_msg_duplicate
import fr.outadoc.justchatting.shared.irc_notice_msg_emoteonly
import fr.outadoc.justchatting.shared.irc_notice_msg_followersonly
import fr.outadoc.justchatting.shared.irc_notice_msg_followersonly_followed
import fr.outadoc.justchatting.shared.irc_notice_msg_followersonly_zero
import fr.outadoc.justchatting.shared.irc_notice_msg_r9k
import fr.outadoc.justchatting.shared.irc_notice_msg_ratelimit
import fr.outadoc.justchatting.shared.irc_notice_msg_rejected
import fr.outadoc.justchatting.shared.irc_notice_msg_rejected_mandatory
import fr.outadoc.justchatting.shared.irc_notice_msg_slowmode
import fr.outadoc.justchatting.shared.irc_notice_msg_subsonly
import fr.outadoc.justchatting.shared.irc_notice_msg_suspended
import fr.outadoc.justchatting.shared.irc_notice_msg_timedout
import fr.outadoc.justchatting.shared.irc_notice_msg_verified_email
import fr.outadoc.justchatting.shared.irc_notice_no_help
import fr.outadoc.justchatting.shared.irc_notice_no_mods
import fr.outadoc.justchatting.shared.irc_notice_no_permission
import fr.outadoc.justchatting.shared.irc_notice_no_vips
import fr.outadoc.justchatting.shared.irc_notice_not_hosting
import fr.outadoc.justchatting.shared.irc_notice_r9k_off
import fr.outadoc.justchatting.shared.irc_notice_r9k_on
import fr.outadoc.justchatting.shared.irc_notice_raid_error_already_raiding
import fr.outadoc.justchatting.shared.irc_notice_raid_error_forbidden
import fr.outadoc.justchatting.shared.irc_notice_raid_error_self
import fr.outadoc.justchatting.shared.irc_notice_raid_error_too_many_viewers
import fr.outadoc.justchatting.shared.irc_notice_raid_error_unexpected
import fr.outadoc.justchatting.shared.irc_notice_raid_notice_mature
import fr.outadoc.justchatting.shared.irc_notice_raid_notice_restricted_chat
import fr.outadoc.justchatting.shared.irc_notice_room_mods
import fr.outadoc.justchatting.shared.irc_notice_slow_off
import fr.outadoc.justchatting.shared.irc_notice_slow_on
import fr.outadoc.justchatting.shared.irc_notice_subs_off
import fr.outadoc.justchatting.shared.irc_notice_subs_on
import fr.outadoc.justchatting.shared.irc_notice_timeout_no_timeout
import fr.outadoc.justchatting.shared.irc_notice_timeout_success
import fr.outadoc.justchatting.shared.irc_notice_tos_ban
import fr.outadoc.justchatting.shared.irc_notice_turbo_only_color
import fr.outadoc.justchatting.shared.irc_notice_unavailable_command
import fr.outadoc.justchatting.shared.irc_notice_unban_success
import fr.outadoc.justchatting.shared.irc_notice_unmod_success
import fr.outadoc.justchatting.shared.irc_notice_unraid_error_no_active_raid
import fr.outadoc.justchatting.shared.irc_notice_unraid_error_unexpected
import fr.outadoc.justchatting.shared.irc_notice_unraid_success
import fr.outadoc.justchatting.shared.irc_notice_unrecognized_cmd
import fr.outadoc.justchatting.shared.irc_notice_untimeout_banned
import fr.outadoc.justchatting.shared.irc_notice_untimeout_success
import fr.outadoc.justchatting.shared.irc_notice_unvip_success
import fr.outadoc.justchatting.shared.irc_notice_usage_ban
import fr.outadoc.justchatting.shared.irc_notice_usage_clear
import fr.outadoc.justchatting.shared.irc_notice_usage_color
import fr.outadoc.justchatting.shared.irc_notice_usage_commercial
import fr.outadoc.justchatting.shared.irc_notice_usage_delete
import fr.outadoc.justchatting.shared.irc_notice_usage_disconnect
import fr.outadoc.justchatting.shared.irc_notice_usage_emote_only_off
import fr.outadoc.justchatting.shared.irc_notice_usage_emote_only_on
import fr.outadoc.justchatting.shared.irc_notice_usage_followers_off
import fr.outadoc.justchatting.shared.irc_notice_usage_followers_on
import fr.outadoc.justchatting.shared.irc_notice_usage_help
import fr.outadoc.justchatting.shared.irc_notice_usage_host
import fr.outadoc.justchatting.shared.irc_notice_usage_marker
import fr.outadoc.justchatting.shared.irc_notice_usage_me
import fr.outadoc.justchatting.shared.irc_notice_usage_mod
import fr.outadoc.justchatting.shared.irc_notice_usage_mods
import fr.outadoc.justchatting.shared.irc_notice_usage_r9k_off
import fr.outadoc.justchatting.shared.irc_notice_usage_r9k_on
import fr.outadoc.justchatting.shared.irc_notice_usage_raid
import fr.outadoc.justchatting.shared.irc_notice_usage_slow_off
import fr.outadoc.justchatting.shared.irc_notice_usage_slow_on
import fr.outadoc.justchatting.shared.irc_notice_usage_subs_off
import fr.outadoc.justchatting.shared.irc_notice_usage_subs_on
import fr.outadoc.justchatting.shared.irc_notice_usage_timeout
import fr.outadoc.justchatting.shared.irc_notice_usage_unban
import fr.outadoc.justchatting.shared.irc_notice_usage_unhost
import fr.outadoc.justchatting.shared.irc_notice_usage_unmod
import fr.outadoc.justchatting.shared.irc_notice_usage_unraid
import fr.outadoc.justchatting.shared.irc_notice_usage_untimeout
import fr.outadoc.justchatting.shared.irc_notice_usage_unvip
import fr.outadoc.justchatting.shared.irc_notice_usage_user
import fr.outadoc.justchatting.shared.irc_notice_usage_vip
import fr.outadoc.justchatting.shared.irc_notice_usage_vips
import fr.outadoc.justchatting.shared.irc_notice_usage_whisper
import fr.outadoc.justchatting.shared.irc_notice_vip_success
import fr.outadoc.justchatting.shared.irc_notice_vips_success
import fr.outadoc.justchatting.shared.irc_notice_whisper_banned
import fr.outadoc.justchatting.shared.irc_notice_whisper_banned_recipient
import fr.outadoc.justchatting.shared.irc_notice_whisper_invalid_login
import fr.outadoc.justchatting.shared.irc_notice_whisper_invalid_self
import fr.outadoc.justchatting.shared.irc_notice_whisper_limit_per_min
import fr.outadoc.justchatting.shared.irc_notice_whisper_limit_per_sec
import fr.outadoc.justchatting.shared.irc_notice_whisper_restricted
import fr.outadoc.justchatting.shared.irc_notice_whisper_restricted_recipient
import fr.outadoc.justchatting.shared.months
import fr.outadoc.justchatting.shared.user_redeemed
import fr.outadoc.justchatting.shared.viewers
import fr.outadoc.justchatting.utils.presentation.formatNumber
import fr.outadoc.justchatting.utils.resources.StringDesc
import fr.outadoc.justchatting.utils.resources.desc
import kotlinx.collections.immutable.toImmutableList

internal class ChatEventViewMapper {

    fun map(command: ChatEvent): List<ChatListItem> {
        return when (command) {
            is ChatEvent.Message -> {
                listOf(mapMessage(command))
            }

            is ChatEvent.Command.UserState -> {
                listOf(
                    ChatListItem.UserState(
                        emoteSets = command.emoteSets.toImmutableList(),
                    ),
                )
            }

            is ChatEvent.Command.RoomStateDelta -> {
                listOf(
                    ChatListItem.RoomStateDelta(
                        isEmoteOnly = command.isEmoteOnly,
                        minFollowDuration = command.minFollowDuration,
                        uniqueMessagesOnly = command.uniqueMessagesOnly,
                        slowModeDuration = command.slowModeDuration,
                        isSubOnly = command.isSubOnly,
                    ),
                )
            }

            is ChatEvent.Command.ClearChat -> {
                listOf(
                    ChatListItem.RemoveContent(
                        upUntil = command.timestamp,
                        matchingUserId = command.targetUserId,
                    ),
                    if (command.targetUserLogin != null) {
                        if (command.duration == null) {
                            ChatListItem.Message.Highlighted(
                                timestamp = command.timestamp,
                                metadata = ChatListItem.Message.Highlighted.Metadata(
                                    title = command.targetUserLogin.desc(),
                                    titleIcon = Icon.Gavel,
                                    subtitle = Res.string.chat_ban.desc(),
                                ),
                                body = null,
                            )
                        } else {
                            ChatListItem.Message.Highlighted(
                                timestamp = command.timestamp,
                                metadata = ChatListItem.Message.Highlighted.Metadata(
                                    title = command.targetUserLogin.desc(),
                                    titleIcon = Icon.Gavel,
                                    subtitle = Res.string.chat_timeout.desc(command.duration),
                                ),
                                body = null,
                            )
                        }
                    } else {
                        ChatListItem.Message.Notice(
                            timestamp = command.timestamp,
                            text = Res.string.chat_clear.desc(),
                        )
                    },
                )
            }

            is ChatEvent.Command.ClearMessage -> {
                listOf(
                    ChatListItem.RemoveContent(
                        upUntil = command.timestamp,
                        matchingMessageId = command.targetMessageId,
                    ),
                )
            }

            ChatEvent.Command.Ping -> {
                emptyList()
            }
        }
    }

    private fun mapMessage(chatEvent: ChatEvent.Message): ChatListItem = with(chatEvent) {
        when (this) {
            is ChatEvent.Message.Notice -> {
                ChatListItem.Message.Notice(
                    timestamp = timestamp,
                    text = getLabelForNotice(messageId = messageId, message = message)
                        ?: message.desc(),
                )
            }

            is ChatEvent.Message.IncomingRaid -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = Icon.CallReceived,
                        subtitle = Res.string.chat_raid_header
                            .desc(
                                Res.plurals.viewers.desc(
                                    number = raidersCount,
                                    raidersCount,
                                ),
                            ),
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.CancelledRaid -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = Icon.Cancel,
                        subtitle = Res.string.chat_unraid_subtitle.desc(),
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.HighlightedMessage -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = Res.string.irc_msgid_highlighted_message.desc(),
                        titleIcon = Icon.Highlight,
                        subtitle = null,
                    ),
                    body = userMessage.map(),
                )
            }

            is ChatEvent.Message.Announcement -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = Res.string.irc_msgid_announcement.desc(),
                        titleIcon = Icon.Campaign,
                        subtitle = null,
                    ),
                    body = userMessage.map(),
                )
            }

            is ChatEvent.Message.Subscription -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = when (subscriptionPlan) {
                            SUB_PRIME -> Icon.Star
                            else -> Icon.Star
                        },
                        subtitle = when (streakMonths) {
                            0 -> {
                                Res.string.chat_sub_header_withDuration
                                    .desc(
                                        parseSubscriptionTier(subscriptionPlan),
                                        Res.plurals.months.desc(
                                            number = cumulativeMonths,
                                            cumulativeMonths.formatNumber(),
                                        ),
                                    )
                            }

                            else -> {
                                Res.string.chat_sub_header_withDurationAndStreak.desc(
                                    parseSubscriptionTier(subscriptionPlan),
                                    Res.plurals.months.desc(
                                        number = cumulativeMonths,
                                        cumulativeMonths.formatNumber(),
                                    ),
                                    Res.plurals.months.desc(
                                        number = streakMonths,
                                        streakMonths.formatNumber(),
                                    ),
                                )
                            }
                        },
                    ),
                    body = userMessage?.map(),
                )
            }

            is ChatEvent.Message.SubscriptionConversion -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = Icon.Star,
                        subtitle = Res.string.chat_subConversion_header
                            .desc(parseSubscriptionTierWithArticle(subscriptionPlan)),
                    ),
                    body = userMessage?.map(),
                )
            }

            is ChatEvent.Message.MassSubscriptionGift -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = Icon.VolunteerActivism,
                        subtitle = Res.string.chat_massSubGift_header
                            .desc(
                                giftCount.formatNumber(),
                                parseSubscriptionTierWithArticle(subscriptionPlan),
                                totalChannelGiftCount.formatNumber(),
                            ),
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.SubscriptionGift -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    body = null,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = Icon.Redeem,
                        subtitle = Res.string.chat_subGift_header
                            .desc(
                                parseSubscriptionTier(subscriptionPlan),
                                recipientDisplayName,
                                Res.plurals.months.desc(
                                    number = cumulativeMonths,
                                    cumulativeMonths.formatNumber(),
                                ),
                            ),
                    ),
                )
            }

            is ChatEvent.Message.GiftPayForward -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = userDisplayName.desc(),
                        titleIcon = Icon.FastForward,
                        subtitle = when (priorGifterDisplayName) {
                            null -> {
                                Res.string.chat_subGift_payForwardAnonymous.desc()
                            }

                            else -> {
                                Res.string.chat_subGift_payForward.desc(priorGifterDisplayName)
                            }
                        },
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.UserNotice -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    body = userMessage?.map(),
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = systemMsg.desc(),
                        titleIcon = null,
                        subtitle = null,
                    ),
                )
            }

            is ChatEvent.Message.Join -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = Res.string.chat_join.desc(channelLogin),
                        subtitle = null,
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.SendError -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = Res.string.chat_send_msg_error.desc(),
                        subtitle = null,
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.ChatMessage -> {
                val metadata = when {
                    isFirstMessageByUser -> {
                        ChatListItem.Message.Highlighted.Metadata(
                            title = Res.string.chat_first.desc(),
                            titleIcon = Icon.WavingHand,
                            subtitle = null,
                        )
                    }

                    rewardId != null -> {
                        ChatListItem.Message.Highlighted.Metadata(
                            title = Res.string.chat_reward.desc(),
                            titleIcon = Icon.Toll,
                            subtitle = null,
                        )
                    }

                    else -> {
                        null
                    }
                }

                if (metadata != null) {
                    ChatListItem.Message.Highlighted(
                        timestamp = timestamp,
                        body = map(),
                        metadata = metadata,
                    )
                } else {
                    ChatListItem.Message.Simple(
                        body = map(),
                        timestamp = timestamp,
                    )
                }
            }

            is ChatEvent.Message.BroadcastSettingsUpdate -> {
                ChatListItem.BroadcastSettingsUpdate(
                    streamTitle = streamTitle,
                    streamCategory = StreamCategory(
                        id = categoryId,
                        name = categoryName,
                    ),
                )
            }

            is ChatEvent.Message.PinnedMessageUpdate -> {
                ChatListItem.PinnedMessageUpdate(
                    pinnedMessage = pinnedMessage,
                )
            }

            is ChatEvent.Message.PollUpdate -> {
                ChatListItem.PollUpdate(
                    poll = poll,
                )
            }

            is ChatEvent.Message.PredictionUpdate -> {
                ChatListItem.PredictionUpdate(
                    prediction = prediction,
                )
            }

            is ChatEvent.Message.RaidUpdate -> {
                ChatListItem.RaidUpdate(
                    raid = raid,
                )
            }

            is ChatEvent.Message.RedemptionUpdate -> {
                ChatListItem.Message.Highlighted(
                    timestamp = timestamp,
                    metadata = ChatListItem.Message.Highlighted.Metadata(
                        title = Res.plurals.user_redeemed
                            .desc(
                                number = redemption.reward.cost,
                                redemption.userDisplayName,
                                redemption.reward.title,
                                redemption.reward.cost,
                            ),
                        titleIcon = Icon.Toll,
                        subtitle = null,
                    ),
                    body = null,
                )
            }

            is ChatEvent.Message.RichEmbed -> {
                ChatListItem.RichEmbed(
                    messageId = messageId,
                    title = title,
                    requestUrl = requestUrl,
                    thumbnailUrl = thumbnailUrl,
                    authorName = authorName,
                    channelName = channelName,
                )
            }

            is ChatEvent.Message.ViewerCountUpdate -> {
                ChatListItem.ViewerCountUpdate(
                    viewerCount = viewerCount,
                )
            }
        }
    }

    private fun parseSubscriptionTier(planId: String): StringDesc {
        return when (planId) {
            SUB_T1 -> Res.string.chat_sub_tier1.desc()
            SUB_T2 -> Res.string.chat_sub_tier2.desc()
            SUB_T3 -> Res.string.chat_sub_tier3.desc()
            SUB_PRIME -> Res.string.chat_sub_prime.desc()
            else -> planId.desc()
        }
    }

    private fun parseSubscriptionTierWithArticle(planId: String): StringDesc {
        return when (planId) {
            SUB_T1 -> Res.string.chat_subGift_tier1.desc()
            SUB_T2 -> Res.string.chat_subGift_tier2.desc()
            SUB_T3 -> Res.string.chat_subGift_tier3.desc()
            else -> planId.desc()
        }
    }

    private fun ChatEvent.Message.ChatMessage.map(): ChatListItem.Message.Body {
        val mentions = message.orEmpty().getMentionsPrefix()
        val mentionsLength = mentions.sumOf { mention -> mention.length + 1 }

        return ChatListItem.Message.Body(
            message = message.orEmpty()
                .drop(mentionsLength)
                .removePrefix(" "),
            messageId = id,
            chatter = Chatter(
                id = userId,
                displayName = userName,
                login = userLogin,
            ),
            isAction = isAction,
            color = color,
            embeddedEmotes = embeddedEmotes.toImmutableList(),
            badges = badges.orEmpty().toImmutableList(),
            inReplyTo = if (mentions.isNotEmpty()) {
                ChatListItem.Message.Body.InReplyTo(
                    message = inReplyTo?.message,
                    mentions = mentions
                        .map { mention ->
                            mention.drop(1)
                        }
                        .toImmutableList(),
                )
            } else {
                null
            },
        )
    }

    private fun getLabelForNotice(messageId: String?, message: String?): StringDesc? {
        return when (messageId) {
            "already_banned" -> {
                Res.string.irc_notice_already_banned
                    .desc(
                        message?.substringBefore(" is already banned", "") ?: "",
                    )
            }

            "already_emote_only_off" -> {
                Res.string.irc_notice_already_emote_only_off.desc()
            }

            "already_emote_only_on" -> {
                Res.string.irc_notice_already_emote_only_on.desc()
            }

            "already_followers_off" -> {
                Res.string.irc_notice_already_followers_off.desc()
            }

            "already_followers_on" -> {
                Res.string.irc_notice_already_followers_on.desc(
                    message?.substringAfter("is already in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                )
            }

            "already_r9k_off" -> {
                Res.string.irc_notice_already_r9k_off.desc()
            }

            "already_r9k_on" -> {
                Res.string.irc_notice_already_r9k_on.desc()
            }

            "already_slow_off" -> {
                Res.string.irc_notice_already_slow_off.desc()
            }

            "already_slow_on" -> {
                Res.string.irc_notice_already_slow_on.desc(
                    message?.substringAfter("is already in ", "")
                        ?.substringBefore("-second slow", "")
                        ?: "",
                )
            }

            "already_subs_off" -> {
                Res.string.irc_notice_already_subs_off.desc()
            }

            "already_subs_on" -> {
                Res.string.irc_notice_already_subs_on.desc()
            }

            "autohost_receive" -> {
                Res.string.irc_notice_autohost_receive.desc(
                    message?.substringBefore(" is now auto hosting", "") ?: "",
                    message?.substringAfter("you for up to ", "")?.substringBefore(" viewers", "")
                        ?: "",
                )
            }

            "bad_ban_admin" -> {
                Res.string.irc_notice_bad_ban_admin.desc(
                    message?.substringAfter("cannot ban admin", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "bad_ban_anon" -> {
                Res.string.irc_notice_bad_ban_anon.desc()
            }

            "bad_ban_broadcaster" -> {
                Res.string.irc_notice_bad_ban_broadcaster.desc()
            }

            "bad_ban_mod" -> {
                Res.string.irc_notice_bad_ban_mod.desc(
                    message?.substringAfter("cannot ban moderator", "")
                        ?.substringBefore(" unless you are", "") ?: "",
                )
            }

            "bad_ban_self" -> {
                Res.string.irc_notice_bad_ban_self.desc()
            }

            "bad_ban_staff" -> {
                Res.string.irc_notice_bad_ban_staff.desc(
                    message?.substringAfter("cannot ban staff", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "bad_commercial_error" -> {
                Res.string.irc_notice_bad_commercial_error.desc()
            }

            "bad_delete_message_broadcaster" -> {
                Res.string.irc_notice_bad_delete_message_broadcaster.desc()
            }

            "bad_delete_message_mod" -> {
                Res.string.irc_notice_bad_delete_message_mod.desc(
                    message?.substringAfter("from another moderator ", "")
                        ?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "bad_host_error" -> {
                Res.string.irc_notice_bad_host_error.desc(
                    message?.substringAfter("a problem hosting ", "")
                        ?.substringBefore(". Please try", "") ?: "",
                )
            }

            "bad_host_hosting" -> {
                Res.string.irc_notice_bad_host_hosting.desc(
                    message?.substringAfter("is already hosting ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "bad_host_rate_exceeded" -> {
                Res.string.irc_notice_bad_host_rate_exceeded.desc(
                    message?.substringAfter("changed more than ", "")
                        ?.substringBefore(" times every half", "") ?: "",
                )
            }

            "bad_host_rejected" -> {
                Res.string.irc_notice_bad_host_rejected.desc()
            }

            "bad_host_self" -> {
                Res.string.irc_notice_bad_host_self.desc()
            }

            "bad_mod_banned" -> {
                Res.string.irc_notice_bad_mod_banned.desc(
                    message?.substringBefore(" is banned", "") ?: "",
                )
            }

            "bad_mod_mod" -> {
                Res.string.irc_notice_bad_mod_mod.desc(
                    message?.substringBefore(" is already", "") ?: "",
                )
            }

            "bad_slow_duration" -> {
                Res.string.irc_notice_bad_slow_duration.desc(
                    message?.substringAfter("to more than ", "")?.substringBefore(" seconds.", "")
                        ?: "",
                )
            }

            "bad_timeout_admin" -> {
                Res.string.irc_notice_bad_timeout_admin.desc(
                    message?.substringAfter("cannot timeout admin ", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "bad_timeout_anon" -> {
                Res.string.irc_notice_bad_timeout_anon.desc()
            }

            "bad_timeout_broadcaster" -> {
                Res.string.irc_notice_bad_timeout_broadcaster.desc()
            }

            "bad_timeout_duration" -> {
                Res.string.irc_notice_bad_timeout_duration.desc(
                    message?.substringAfter("for more than ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "bad_timeout_mod" -> {
                Res.string.irc_notice_bad_timeout_mod.desc(
                    message?.substringAfter("cannot timeout moderator ", "")
                        ?.substringBefore(" unless you are", "") ?: "",
                )
            }

            "bad_timeout_self" -> {
                Res.string.irc_notice_bad_timeout_self.desc()
            }

            "bad_timeout_staff" -> {
                Res.string.irc_notice_bad_timeout_staff.desc(
                    message?.substringAfter("cannot timeout staff ", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "bad_unban_no_ban" -> {
                Res.string.irc_notice_bad_unban_no_ban.desc(
                    message?.substringBefore(" is not banned", "") ?: "",
                )
            }

            "bad_unhost_error" -> {
                Res.string.irc_notice_bad_unhost_error.desc()
            }

            "bad_unmod_mod" -> {
                Res.string.irc_notice_bad_unmod_mod.desc(
                    message?.substringBefore(" is not a", "") ?: "",
                )
            }

            "bad_vip_grantee_banned" -> {
                Res.string.irc_notice_bad_vip_grantee_banned.desc(
                    message?.substringBefore(" is banned in", "") ?: "",
                )
            }

            "bad_vip_grantee_already_vip" -> {
                Res.string.irc_notice_bad_vip_grantee_already_vip.desc(
                    message?.substringBefore(" is already a", "") ?: "",
                )
            }

            "bad_vip_max_vips_reached" -> {
                Res.string.irc_notice_bad_vip_max_vips_reached.desc()
            }

            "bad_vip_achievement_incomplete" -> {
                Res.string.irc_notice_bad_vip_achievement_incomplete.desc()
            }

            "bad_unvip_grantee_not_vip" -> {
                Res.string.irc_notice_bad_unvip_grantee_not_vip.desc(
                    message?.substringBefore(" is not a", "") ?: "",
                )
            }

            "ban_success" -> {
                Res.string.irc_notice_ban_success.desc(
                    message?.substringBefore(" is now banned", "") ?: "",
                )
            }

            "cmds_available" -> {
                Res.string.irc_notice_cmds_available.desc(
                    message?.substringAfter("details): ", "")?.substringBefore(" More help:", "")
                        ?: "",
                )
            }

            "color_changed" -> {
                Res.string.irc_notice_color_changed.desc()
            }

            "commercial_success" -> {
                Res.string.irc_notice_commercial_success.desc(
                    message?.substringAfter("Initiating ", "")
                        ?.substringBefore(" second commercial break.", "") ?: "",
                )
            }

            "delete_message_success" -> {
                Res.string.irc_notice_delete_message_success.desc(
                    message?.substringAfter("The message from ", "")
                        ?.substringBefore(" is now deleted.", "") ?: "",
                )
            }

            "delete_staff_message_success" -> {
                Res.string.irc_notice_delete_staff_message_success.desc(
                    message?.substringAfter("message from staff ", "")
                        ?.substringBefore(". Please email", "") ?: "",
                )
            }

            "emote_only_off" -> {
                Res.string.irc_notice_emote_only_off.desc()
            }

            "emote_only_on" -> {
                Res.string.irc_notice_emote_only_on.desc()
            }

            "followers_off" -> {
                Res.string.irc_notice_followers_off.desc()
            }

            "followers_on" -> {
                Res.string.irc_notice_followers_on.desc(
                    message?.substringAfter("is now in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                )
            }

            "followers_on_zero" -> {
                Res.string.irc_notice_followers_on_zero.desc()
            }

            "host_off" -> {
                Res.string.irc_notice_host_off.desc()
            }

            "host_on" -> {
                Res.string.irc_notice_host_on.desc(
                    message?.substringAfter("Now hosting ", "")?.substringBeforeLast(".", "") ?: "",
                )
            }

            "host_receive" -> {
                Res.string.irc_notice_host_receive.desc(
                    message?.substringBefore(" is now hosting", "") ?: "",
                    message?.substringAfter("you for up to ", "")?.substringBefore(" viewers", "")
                        ?: "",
                )
            }

            "host_receive_no_count" -> {
                Res.string.irc_notice_host_receive_no_count.desc(
                    message?.substringBefore(" is now hosting", "") ?: "",
                )
            }

            "host_target_went_offline" -> {
                Res.string.irc_notice_host_target_went_offline.desc(
                    message?.substringBefore(" has gone offline", "") ?: "",
                )
            }

            "hosts_remaining" -> {
                Res.string.irc_notice_hosts_remaining.desc(
                    message?.substringBefore(" host commands", "") ?: "",
                )
            }

            "invalid_user" -> {
                Res.string.irc_notice_invalid_user.desc(
                    message?.substringAfter("Invalid username: ", "") ?: "",
                )
            }

            "mod_success" -> {
                Res.string.irc_notice_mod_success.desc(
                    message?.substringAfter("You have added ", "")
                        ?.substringBefore(" as a moderator", "") ?: "",
                )
            }

            "msg_banned" -> {
                Res.string.irc_notice_msg_banned.desc(
                    message?.substringAfter("from talking in ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "msg_bad_characters" -> {
                Res.string.irc_notice_msg_bad_characters.desc()
            }

            "msg_channel_blocked" -> {
                Res.string.irc_notice_msg_channel_blocked.desc()
            }

            "msg_channel_suspended" -> {
                Res.string.irc_notice_msg_channel_suspended.desc()
            }

            "msg_duplicate" -> {
                Res.string.irc_notice_msg_duplicate.desc()
            }

            "msg_emoteonly" -> {
                Res.string.irc_notice_msg_emoteonly.desc()
            }

            "msg_followersonly" -> {
                Res.string.irc_notice_msg_followersonly.desc(
                    message?.substringAfter("This room is in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                    message?.substringAfter("Follow ", "")?.substringBefore(" to join", "") ?: "",
                )
            }

            "msg_followersonly_followed" -> {
                Res.string.irc_notice_msg_followersonly_followed.desc(
                    message?.substringAfter("This room is in ", "")
                        ?.substringBefore(" followers-only mode", "") ?: "",
                    message?.substringAfter("following for ", "")?.substringBefore(". Continue", "")
                        ?: "",
                )
            }

            "msg_followersonly_zero" -> {
                Res.string.irc_notice_msg_followersonly_zero.desc(
                    message?.substringAfter(". Follow ", "")?.substringBefore(" to join the", "")
                        ?: "",
                )
            }

            "msg_r9k" -> {
                Res.string.irc_notice_msg_r9k.desc()
            }

            "msg_ratelimit" -> {
                Res.string.irc_notice_msg_ratelimit.desc()
            }

            "msg_rejected" -> {
                Res.string.irc_notice_msg_rejected.desc()
            }

            "msg_rejected_mandatory" -> {
                Res.string.irc_notice_msg_rejected_mandatory.desc()
            }

            "msg_slowmode" -> {
                Res.string.irc_notice_msg_slowmode.desc(
                    message?.substringAfter("talk again in ", "")?.substringBefore(" seconds.", "")
                        ?: "",
                )
            }

            "msg_subsonly" -> {
                Res.string.irc_notice_msg_subsonly.desc(
                    message?.substringAfter("/products/", "")?.substringBefore("/ticket?ref", "")
                        ?: "",
                )
            }

            "msg_suspended" -> {
                Res.string.irc_notice_msg_suspended.desc()
            }

            "msg_timedout" -> {
                Res.string.irc_notice_msg_timedout.desc(
                    message?.substringAfter("timed out for ", "")
                        ?.substringBefore(" more seconds.", "")
                        ?: "",
                )
            }

            "msg_verified_email" -> {
                Res.string.irc_notice_msg_verified_email.desc()
            }

            "no_help" -> {
                Res.string.irc_notice_no_help.desc()
            }

            "no_mods" -> {
                Res.string.irc_notice_no_mods.desc()
            }

            "no_vips" -> {
                Res.string.irc_notice_no_vips.desc()
            }

            "not_hosting" -> {
                Res.string.irc_notice_not_hosting.desc()
            }

            "no_permission" -> {
                Res.string.irc_notice_no_permission.desc()
            }

            "r9k_off" -> {
                Res.string.irc_notice_r9k_off.desc()
            }

            "r9k_on" -> {
                Res.string.irc_notice_r9k_on.desc()
            }

            "raid_error_already_raiding" -> {
                Res.string.irc_notice_raid_error_already_raiding.desc()
            }

            "raid_error_forbidden" -> {
                Res.string.irc_notice_raid_error_forbidden.desc()
            }

            "raid_error_self" -> {
                Res.string.irc_notice_raid_error_self.desc()
            }

            "raid_error_too_many_viewers" -> {
                Res.string.irc_notice_raid_error_too_many_viewers.desc()
            }

            "raid_error_unexpected" -> {
                Res.string.irc_notice_raid_error_unexpected.desc(
                    message?.substringAfter("a problem raiding ", "")
                        ?.substringBefore(". Please try", "") ?: "",
                )
            }

            "raid_notice_mature" -> {
                Res.string.irc_notice_raid_notice_mature.desc()
            }

            "raid_notice_restricted_chat" -> {
                Res.string.irc_notice_raid_notice_restricted_chat.desc()
            }

            "room_mods" -> {
                Res.string.irc_notice_room_mods.desc(
                    message?.substringAfter("this channel are: ", "") ?: "",
                )
            }

            "slow_off" -> {
                Res.string.irc_notice_slow_off.desc()
            }

            "slow_on" -> {
                Res.string.irc_notice_slow_on.desc(
                    message?.substringAfter("send messages every ", "")
                        ?.substringBefore(" seconds.", "") ?: "",
                )
            }

            "subs_off" -> {
                Res.string.irc_notice_subs_off.desc()
            }

            "subs_on" -> {
                Res.string.irc_notice_subs_on.desc()
            }

            "timeout_no_timeout" -> {
                Res.string.irc_notice_timeout_no_timeout.desc(
                    message?.substringBefore(" is not timed", "") ?: "",
                )
            }

            "timeout_success" -> {
                Res.string.irc_notice_timeout_success.desc(
                    message?.substringBefore(" has been", "") ?: "",
                    message?.substringAfter("timed out for ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "tos_ban" -> {
                Res.string.irc_notice_tos_ban.desc(
                    message?.substringAfter("has closed channel ", "")
                        ?.substringBefore(" due to Terms", "") ?: "",
                )
            }

            "turbo_only_color" -> {
                Res.string.irc_notice_turbo_only_color.desc(
                    message?.substringAfter("following instead: ", "") ?: "",
                )
            }

            "unavailable_command" -> {
                Res.string.irc_notice_unavailable_command.desc(
                    message?.substringAfter("Sorry, “", "")
                        ?.substringBefore("” is not available", "")
                        ?: "",
                )
            }

            "unban_success" -> {
                Res.string.irc_notice_unban_success.desc(
                    message?.substringBefore(" is no longer", "") ?: "",
                )
            }

            "unmod_success" -> {
                Res.string.irc_notice_unmod_success.desc(
                    message?.substringAfter("You have removed ", "")
                        ?.substringBefore(" as a moderator", "") ?: "",
                )
            }

            "unraid_error_no_active_raid" -> {
                Res.string.irc_notice_unraid_error_no_active_raid.desc()
            }

            "unraid_error_unexpected" -> {
                Res.string.irc_notice_unraid_error_unexpected.desc()
            }

            "unraid_success" -> {
                Res.string.irc_notice_unraid_success.desc()
            }

            "unrecognized_cmd" -> {
                Res.string.irc_notice_unrecognized_cmd.desc(
                    message?.substringAfter("Unrecognized command: ", "") ?: "",
                )
            }

            "untimeout_banned" -> {
                Res.string.irc_notice_untimeout_banned.desc(
                    message?.substringBefore(" is permanently banned", "") ?: "",
                )
            }

            "untimeout_success" -> {
                Res.string.irc_notice_untimeout_success.desc(
                    message?.substringBefore(" is no longer", "") ?: "",
                )
            }

            "unvip_success" -> {
                Res.string.irc_notice_unvip_success.desc(
                    message?.substringAfter("You have removed ", "")
                        ?.substringBefore(" as a VIP", "")
                        ?: "",
                )
            }

            "usage_ban" -> {
                Res.string.irc_notice_usage_ban.desc()
            }

            "usage_clear" -> {
                Res.string.irc_notice_usage_clear.desc()
            }

            "usage_color" -> {
                Res.string.irc_notice_usage_color.desc(
                    message?.substringAfter("following: ", "")?.substringBeforeLast(".", "") ?: "",
                )
            }

            "usage_commercial" -> {
                Res.string.irc_notice_usage_commercial.desc()
            }

            "usage_disconnect" -> {
                Res.string.irc_notice_usage_disconnect.desc()
            }

            "usage_delete" -> {
                Res.string.irc_notice_usage_delete.desc()
            }

            "usage_emote_only_off" -> {
                Res.string.irc_notice_usage_emote_only_off.desc()
            }

            "usage_emote_only_on" -> {
                Res.string.irc_notice_usage_emote_only_on.desc()
            }

            "usage_followers_off" -> {
                Res.string.irc_notice_usage_followers_off.desc()
            }

            "usage_followers_on" -> {
                Res.string.irc_notice_usage_followers_on.desc()
            }

            "usage_help" -> {
                Res.string.irc_notice_usage_help.desc()
            }

            "usage_host" -> {
                Res.string.irc_notice_usage_host.desc()
            }

            "usage_marker" -> {
                Res.string.irc_notice_usage_marker.desc()
            }

            "usage_me" -> {
                Res.string.irc_notice_usage_me.desc()
            }

            "usage_mod" -> {
                Res.string.irc_notice_usage_mod.desc()
            }

            "usage_mods" -> {
                Res.string.irc_notice_usage_mods.desc()
            }

            "usage_r9k_off" -> {
                Res.string.irc_notice_usage_r9k_off.desc()
            }

            "usage_r9k_on" -> {
                Res.string.irc_notice_usage_r9k_on.desc()
            }

            "usage_raid" -> {
                Res.string.irc_notice_usage_raid.desc()
            }

            "usage_slow_off" -> {
                Res.string.irc_notice_usage_slow_off.desc()
            }

            "usage_slow_on" -> {
                Res.string.irc_notice_usage_slow_on.desc(
                    message?.substringAfter("default=", "")?.substringBefore(")", "") ?: "",
                )
            }

            "usage_subs_off" -> {
                Res.string.irc_notice_usage_subs_off.desc()
            }

            "usage_subs_on" -> {
                Res.string.irc_notice_usage_subs_on.desc()
            }

            "usage_timeout" -> {
                Res.string.irc_notice_usage_timeout.desc()
            }

            "usage_unban" -> {
                Res.string.irc_notice_usage_unban.desc()
            }

            "usage_unhost" -> {
                Res.string.irc_notice_usage_unhost.desc()
            }

            "usage_unmod" -> {
                Res.string.irc_notice_usage_unmod.desc()
            }

            "usage_unraid" -> {
                Res.string.irc_notice_usage_unraid.desc()
            }

            "usage_untimeout" -> {
                Res.string.irc_notice_usage_untimeout.desc()
            }

            "usage_unvip" -> {
                Res.string.irc_notice_usage_unvip.desc()
            }

            "usage_user" -> {
                Res.string.irc_notice_usage_user.desc()
            }

            "usage_vip" -> {
                Res.string.irc_notice_usage_vip.desc()
            }

            "usage_vips" -> {
                Res.string.irc_notice_usage_vips.desc()
            }

            "usage_whisper" -> {
                Res.string.irc_notice_usage_whisper.desc()
            }

            "vip_success" -> {
                Res.string.irc_notice_vip_success.desc(
                    message?.substringAfter("You have added ", "")
                        ?.substringBeforeLast(" as a vip", "")
                        ?: "",
                )
            }

            "vips_success" -> {
                Res.string.irc_notice_vips_success.desc(
                    message?.substringAfter("channel are: ", "")?.substringBeforeLast(".", "")
                        ?: "",
                )
            }

            "whisper_banned" -> {
                Res.string.irc_notice_whisper_banned.desc()
            }

            "whisper_banned_recipient" -> {
                Res.string.irc_notice_whisper_banned_recipient.desc()
            }

            "whisper_invalid_login" -> {
                Res.string.irc_notice_whisper_invalid_login.desc()
            }

            "whisper_invalid_self" -> {
                Res.string.irc_notice_whisper_invalid_self.desc()
            }

            "whisper_limit_per_min" -> {
                Res.string.irc_notice_whisper_limit_per_min.desc()
            }

            "whisper_limit_per_sec" -> {
                Res.string.irc_notice_whisper_limit_per_sec.desc()
            }

            "whisper_restricted" -> {
                Res.string.irc_notice_whisper_restricted.desc()
            }

            "whisper_restricted_recipient" -> {
                Res.string.irc_notice_whisper_restricted_recipient.desc()
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

private fun String.getMentionsPrefix(): List<String> {
    return split(" ")
        .takeWhile { word ->
            word.startsWith(ChatPrefixConstants.ChatterPrefix)
        }
}
