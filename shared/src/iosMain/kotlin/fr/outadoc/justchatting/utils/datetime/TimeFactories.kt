package fr.outadoc.justchatting.utils.datetime

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

// Lets Swift build Kotlin time values, e.g. for SwiftUI preview data. Duration is a value class
// that Objective-C export turns into an opaque Long, so Swift must not build it by hand.

public fun instantFromEpochSeconds(epochSeconds: Long): Instant = Instant.fromEpochSeconds(epochSeconds)

public fun durationFromSeconds(seconds: Long): Duration = seconds.seconds
