package fr.outadoc.justchatting.utils.core

import com.google.gson.JsonElement

val JsonElement.asStringOrNull: String?
    get() = takeUnless { it.isJsonNull }?.asString
