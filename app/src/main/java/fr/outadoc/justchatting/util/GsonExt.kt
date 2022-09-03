package fr.outadoc.justchatting.util

import com.google.gson.JsonElement

val JsonElement.asStringOrNull: String?
    get() = takeUnless { it.isJsonNull }?.asString
