package fr.outadoc.justchatting.model.helix.follows

enum class Order(val value: String) {
    ASC("asc"),
    DESC("desc");

    override fun toString() = value
}
