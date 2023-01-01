package fr.outadoc.justchatting.feature.chat.data.emotes

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableSet

internal fun <T> flatImmutableSetOf(head: T, vararg lists: List<T>): ImmutableSet<T> {
    return (listOf(head) + lists.flatMap { item -> item }).toImmutableSet()
}
