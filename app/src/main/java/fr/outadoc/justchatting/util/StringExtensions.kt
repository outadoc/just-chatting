package fr.outadoc.justchatting.util

fun String.nullIfEmpty() = takeIf { it.isNotEmpty() }

fun String.withBearerPrefix() = "Bearer $this"
