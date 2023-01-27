package fr.outadoc.justchatting.utils.core

import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

suspend fun delayWithJitter(delay: Duration, maxJitter: Duration) {
    val jitter = Random.nextLong(maxJitter.inWholeMilliseconds).milliseconds
    delay(delay + jitter)
}
