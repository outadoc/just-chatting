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

fun Map<String, String?>.parseEmotes(message: String): List<Emote>? {
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

fun Map<String, String?>.parseBadges(): List<Badge>? =
    this["badges"]
        ?.splitAndMakeMap(",", "/")
        ?.entries
        ?.mapNotNull { (key, value) ->
            value?.let { Badge(key, value) }
        }

fun Map<String, String?>.parseTimestamp(): Instant? {
    val prop = this["tmi-sent-ts"] ?: this["rm-received-ts"]
    return prop?.toLong()?.let { Instant.fromEpochMilliseconds(it) }
}

fun Map<String, String?>.parseParentMessage(): IrcEvent.Message.ChatMessage.InReplyTo? {
    return IrcEvent.Message.ChatMessage.InReplyTo(
        id = this["reply-parent-msg-id"] ?: return null,
        message = this["reply-parent-msg-body"] ?: return null,
        userId = this["reply-parent-user-id"] ?: return null,
        userLogin = this["reply-parent-user-login"] ?: return null,
        userName = this["reply-parent-display-name"] ?: return null,
    )
}

val Map<String, String?>.banDuration: Duration?
    get() = this["ban-duration"]?.toIntOrNull()?.seconds

val Map<String, String?>.color: String?
    get() = this["color"]?.takeUnless { it.isEmpty() }

val Map<String, String?>.customRewardId: String?
    get() = this["custom-reward-id"]?.takeUnless { it.isEmpty() }

val Map<String, String?>.displayName: String?
    get() = this["display-name"]?.trim()?.takeUnless { it.isEmpty() }

val Map<String, String?>.raidersCount: Int?
    get() = this["msg-param-viewerCount"]?.toIntOrNull()

val Map<String, String?>.emoteSets: List<String>?
    get() = this["emote-sets"]?.split(",")?.dropLastWhile { it.isEmpty() }

val Map<String, String?>.firstMsg: Boolean
    get() = this["first-msg"] == "1"

val Map<String, String?>.id: String?
    get() = this["id"]?.takeUnless { it.isEmpty() }

val Map<String, String?>.isEmoteOnly: Boolean?
    get() = when (this["emote-only"]) {
        "1" -> true
        "0" -> false
        else -> null
    }

val Map<String, String?>.minFollowDuration: Duration?
    get() = this["followers-only"]?.toIntOrNull()?.minutes

val Map<String, String?>.slowModeDuration: Duration?
    get() = this["slow"]?.toIntOrNull()?.seconds

val Map<String, String?>.uniqueMessagesOnly: Boolean?
    get() = when (this["r9k"]) {
        "1" -> true
        "0" -> false
        else -> null
    }

val Map<String, String?>.isSubOnly: Boolean?
    get() = when (this["subs-only"]) {
        "1" -> true
        "0" -> false
        else -> null
    }

val Map<String, String?>.login: String?
    get() = this["login"]?.takeUnless { it.isEmpty() }

val Map<String, String?>.messageId: String?
    get() = this["msg-id"]?.takeUnless { it.isEmpty() }

val Map<String, String?>.targetMessageId: String?
    get() = this["target-msg-id"]?.takeUnless { it.isEmpty() }

val Map<String, String?>.targetUserId: String?
    get() = this["target-user-id"]?.takeUnless { it.isEmpty() }

val Map<String, String?>.systemMsg: String?
    get() = this["system-msg"]?.takeUnless { it.isEmpty() }

val Map<String, String?>.userId: String
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
