package fr.outadoc.justchatting.utils.core

sealed class StringOrRes {
    data class Literal(val value: CharSequence) : StringOrRes()
    data class Resource(val resId: Int) : StringOrRes()
}

fun Int.asStringOrRes(): StringOrRes = StringOrRes.Resource(resId = this)
fun CharSequence.asStringOrRes(): StringOrRes = StringOrRes.Literal(value = this)
