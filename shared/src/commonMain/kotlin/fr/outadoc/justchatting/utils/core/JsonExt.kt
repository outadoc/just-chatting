package fr.outadoc.justchatting.utils.core

import kotlinx.serialization.json.Json

val DefaultJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
