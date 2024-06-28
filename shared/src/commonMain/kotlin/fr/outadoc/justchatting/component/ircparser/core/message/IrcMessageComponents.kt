/**
 * Copyright 2018 WillowChat project.
 * This file is distributed under the ISC license.
 * https://github.com/WillowChat/Kale
 */

package fr.outadoc.justchatting.component.ircparser.core.message

internal data class IrcMessageComponents(
    val parameters: List<String> = listOf(),
    val tags: Map<String, String?> = mapOf(),
    val prefix: String? = null,
)
