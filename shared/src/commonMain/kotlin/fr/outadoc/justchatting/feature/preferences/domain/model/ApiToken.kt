package fr.outadoc.justchatting.feature.preferences.domain.model

import kotlin.jvm.JvmInline

@JvmInline
public value class ApiToken(
    public val value: String,
) {
    override fun toString(): String = "***"
}
