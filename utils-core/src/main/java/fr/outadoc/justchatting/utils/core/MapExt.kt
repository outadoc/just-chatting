package fr.outadoc.justchatting.utils.core

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K, V>.filterKeysNotNull(): Map<K & Any, V> {
    return filterKeys { key -> key != null } as Map<K & Any, V>
}

@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K, V>.filterValuesNotNull(): Map<K, V & Any> {
    return filterValues { value -> value != null } as Map<K, V & Any>
}
