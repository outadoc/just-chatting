package fr.outadoc.justchatting.feature.chat.presentation

import kotlin.time.DurationUnit

// kotlin.time.Duration is a value class, which Objective-C export turns into its opaque internal
// Long representation. These accessors expose durations to Swift as plain seconds instead.

/** Minimum follow duration required to chat, in seconds; 0 means any follower can chat. */
public val RoomMode.FollowersOnly.minFollowDurationSeconds: Double
    get() = minFollowDuration.toDouble(DurationUnit.SECONDS)

/** Delay between two messages, in seconds. */
public val RoomMode.Slow.delaySeconds: Double
    get() = delay.toDouble(DurationUnit.SECONDS)

public val MessagePostConstraint.slowModeDurationSeconds: Double
    get() = slowModeDuration.toDouble(DurationUnit.SECONDS)
