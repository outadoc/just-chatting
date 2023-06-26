package fr.outadoc.justchatting.component.twitch.websocket.irc

import fr.outadoc.justchatting.component.chatapi.common.Badge
import fr.outadoc.justchatting.component.chatapi.common.ChatEmote
import fr.outadoc.justchatting.component.chatapi.common.Emote
import fr.outadoc.justchatting.component.twitch.utils.map
import fr.outadoc.justchatting.component.twitch.websocket.irc.model.IrcEvent
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal fun Map<String, String?>.parseEmotes(message: String): List<Emote>? {
    return this["emotes"]
        ?.splitAndMakeMap(split = "/", map = ":")
        ?.entries
        ?.flatMap { emote ->
            emote.value
                ?.split(",")
                ?.map { indexes ->
                    val index = indexes.split("-")
                    val begin = index[0].toInt()
                    val end = index[1].toInt()

                    val realBegin = message.offsetByCodePoints(0, begin)
                    val realEnd = if (begin == realBegin) end else end + realBegin - begin

                    ChatEmote(
                        id = emote.key,
                        name = message.slice(realBegin..realEnd),
                    ).map()
                }
                .orEmpty()
        }
}

internal fun Map<String, String?>.parseBadges(): List<Badge>? =
    this["badges"]
        ?.splitAndMakeMap(",", "/")
        ?.entries
        ?.mapNotNull { (key, value) ->
            value?.let { Badge(key, value) }
        }

internal fun Map<String, String?>.parseTimestamp(): Instant? {
    val prop = this["tmi-sent-ts"] ?: this["rm-received-ts"]
    return prop?.toLong()?.let { Instant.fromEpochMilliseconds(it) }
}

internal fun Map<String, String?>.parseParentMessage(): IrcEvent.Message.ChatMessage.InReplyTo? {
    return IrcEvent.Message.ChatMessage.InReplyTo(
        id = this["reply-parent-msg-id"] ?: return null,
        message = this["reply-parent-msg-body"] ?: return null,
        userId = this["reply-parent-user-id"] ?: return null,
        userLogin = this["reply-parent-user-login"] ?: return null,
        userName = this["reply-parent-display-name"] ?: return null,
    )
}

internal fun Map<String, String?>.parsePaidMessageInfo(): IrcEvent.Message.ChatMessage.PaidMessageInfo? {
    return IrcEvent.Message.ChatMessage.PaidMessageInfo(
        amount = this["pinned-chat-paid-amount"]?.toLongOrNull()
            ?: this["pinned-chat-paid-canonical-amount"]?.toLongOrNull()
            ?: return null,
        currency = this["pinned-chat-paid-currency"] ?: return null,
        exponent = this["pinned-chat-paid-exponent"]?.toIntOrNull() ?: return null,
        isSystemMessage = this["pinned-chat-paid-is-system-message"] == "1",
        level = this["pinned-chat-paid-level"] ?: return null,
    )
}

internal val Map<String, String?>.banDuration: Duration?
    get() = this["ban-duration"]?.toIntOrNull()?.seconds

internal val Map<String, String?>.color: String?
    get() = this["color"]?.takeUnless { it.isEmpty() }

internal val Map<String, String?>.customRewardId: String?
    get() = this["custom-reward-id"]?.takeUnless { it.isEmpty() }

internal val Map<String, String?>.displayName: String?
    get() = this["display-name"]?.trim()?.takeUnless { it.isEmpty() }

internal val Map<String, String?>.raidersCount: Int?
    get() = this["msg-param-viewerCount"]?.toIntOrNull()

internal val Map<String, String?>.multiMonthDuration: Int?
    get() = this["msg-param-multimonth-duration"]?.toIntOrNull()?.coerceAtLeast(1)

internal val Map<String, String?>.giftCumulativeMonths: Int?
    get() = this["msg-param-months"]?.toIntOrNull()

internal val Map<String, String?>.giftMonths: Int?
    get() = this["msg-param-gift-months"]?.toIntOrNull()

internal val Map<String, String?>.cumulativeMonths: Int?
    get() = this["msg-param-cumulative-months"]?.toIntOrNull()

internal val Map<String, String?>.streakMonths: Int?
    get() = this["msg-param-streak-months"]?.toIntOrNull()

internal val Map<String, String?>.subscriptionPlan: String?
    get() = this["msg-param-sub-plan"]?.takeUnless { it.isEmpty() }

internal val Map<String, String?>.recipientDisplayName: String?
    get() = this["msg-param-recipient-display-name"]?.takeUnless { it.isEmpty() }

internal val Map<String, String?>.priorGifterDisplayName: String?
    get() = this["msg-param-prior-gifter-display-name"]?.takeUnless { it.isEmpty() }

internal val Map<String, String?>.priorGifterAnonymous: Boolean
    get() = this["msg-param-prior-gifter-anonymous"] == "true"

internal val Map<String, String?>.massGiftCount: Int?
    get() = this["msg-param-mass-gift-count"]?.toIntOrNull()

internal val Map<String, String?>.totalChannelGiftCount: Int?
    get() = this["msg-param-sender-count"]?.toIntOrNull()

internal val Map<String, String?>.emoteSets: List<String>?
    get() = this["emote-sets"]?.split(",")?.dropLastWhile { it.isEmpty() }

internal val Map<String, String?>.firstMsg: Boolean
    get() = this["first-msg"] == "1"

internal val Map<String, String?>.id: String?
    get() = this["id"]?.takeUnless { it.isEmpty() }

internal val Map<String, String?>.isEmoteOnly: Boolean?
    get() = when (this["emote-only"]) {
        "1" -> true
        "0" -> false
        else -> null
    }

internal val Map<String, String?>.minFollowDuration: Duration?
    get() = this["followers-only"]?.toIntOrNull()?.minutes

internal val Map<String, String?>.slowModeDuration: Duration?
    get() = this["slow"]?.toIntOrNull()?.seconds

internal val Map<String, String?>.uniqueMessagesOnly: Boolean?
    get() = when (this["r9k"]) {
        "1" -> true
        "0" -> false
        else -> null
    }

internal val Map<String, String?>.isSubOnly: Boolean?
    get() = when (this["subs-only"]) {
        "1" -> true
        "0" -> false
        else -> null
    }

internal val Map<String, String?>.login: String?
    get() = this["login"]?.takeUnless { it.isEmpty() }

internal val Map<String, String?>.messageId: String?
    get() = this["msg-id"]?.takeUnless { it.isEmpty() }

internal val Map<String, String?>.targetMessageId: String?
    get() = this["target-msg-id"]?.takeUnless { it.isEmpty() }

internal val Map<String, String?>.targetUserId: String?
    get() = this["target-user-id"]?.takeUnless { it.isEmpty() }

internal val Map<String, String?>.systemMsg: String?
    get() = this["system-msg"]?.takeUnless { it.isEmpty() }

internal val Map<String, String?>.userId: String
    get() = this["user-id"]!!

private fun String.splitAndMakeMap(
    split: String,
    map: String,
): Map<String, String?> = buildMap {
    this@splitAndMakeMap.split(split)
        .dropLastWhile { it.isEmpty() }
        .asSequence()
        .map { pair -> pair.split(map).dropLastWhile { it.isEmpty() } }
        .forEach { this[it[0]] = if (it.size == 2) it[1] else null }
}
