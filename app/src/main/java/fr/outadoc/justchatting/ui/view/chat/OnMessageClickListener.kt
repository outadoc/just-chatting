package fr.outadoc.justchatting.ui.view.chat

fun interface OnMessageClickListener {
    fun send(original: CharSequence, formatted: CharSequence, userId: String?)
}
