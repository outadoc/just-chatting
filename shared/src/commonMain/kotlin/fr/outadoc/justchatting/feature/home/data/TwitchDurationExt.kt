package fr.outadoc.justchatting.feature.home.data

import kotlin.time.Duration

internal fun String.parseTwitchDuration(): Duration {
    return Duration.parse(
        "PT${uppercase()}",
    )
}
