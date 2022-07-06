package com.github.andreyasadchy.xtra.ui.view.chat

fun interface OnMessageClickListener {
    fun send(original: CharSequence, formatted: CharSequence, userId: String?)
}
