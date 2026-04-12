package fr.outadoc.justchatting.utils.core

import kotlinx.serialization.json.Json

internal val DefaultJson: Json =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
