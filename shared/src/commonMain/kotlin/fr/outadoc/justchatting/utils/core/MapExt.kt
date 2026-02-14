package fr.outadoc.justchatting.utils.core

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toPersistentMap

@Suppress("UNCHECKED_CAST")
internal fun <K, V> Map<K, V>.filterKeysNotNull(): Map<K & Any, V> = filterKeys { key -> key != null } as Map<K & Any, V>

@Suppress("UNCHECKED_CAST")
internal fun <K, V> Map<K, V>.filterValuesNotNull(): Map<K, V & Any> = filterValues { value -> value != null } as Map<K, V & Any>

@Suppress("UNCHECKED_CAST")
internal fun <K, V> ImmutableMap<K, V>.filterValuesNotNull(): ImmutableMap<K, V & Any> = (
    filterValues { value ->
        value != null
    } as Map<K, V & Any>
    ).toPersistentMap()
