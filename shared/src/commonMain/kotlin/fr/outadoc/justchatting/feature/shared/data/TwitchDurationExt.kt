package fr.outadoc.justchatting.feature.shared.data

import kotlin.time.Duration

internal fun String.parseTwitchDuration(): Duration {
    return Duration.parse(
        "PT${uppercase()}",
    )
}
