package fr.outadoc.justchatting.feature.chat.presentation

import kotlin.time.DurationUnit

// kotlin.time.Duration is a value class, which Objective-C export turns into its opaque internal
// Long representation. These accessors expose durations to Swift as plain seconds instead.

/** Slow mode delay in seconds, or 0 when slow mode is off. */
public val RoomState.slowModeDurationSeconds: Double
    get() = slowModeDuration.toDouble(DurationUnit.SECONDS)

/**
 * Minimum follow duration required to chat, in seconds; 0 means any follower can chat, and a
 * negative value means followers-only mode is off.
 */
public val RoomState.minFollowDurationSeconds: Double
    get() = minFollowDuration.toDouble(DurationUnit.SECONDS)

public val MessagePostConstraint.slowModeDurationSeconds: Double
    get() = slowModeDuration.toDouble(DurationUnit.SECONDS)
