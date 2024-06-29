package fr.outadoc.justchatting.feature.emotes.domain.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class EmoteUrls(
    val dark: Map<Float, String>,
    val light: Map<Float, String>,
) {
    constructor(url: String) : this(mapOf(1f to url))
    constructor(anyTheme: Map<Float, String>) : this(dark = anyTheme, light = anyTheme)

    init {
        check(dark.isNotEmpty()) { "EmoteUrls.dark is empty" }
        check(light.isNotEmpty()) { "EmoteUrls.light is empty" }
    }
}
