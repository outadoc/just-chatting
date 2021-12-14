package com.github.andreyasadchy.xtra.model.chat

data class Image(
        val url: String,
        var start: Int,
        var end: Int,
        val isEmote: Boolean,
        val type: String = "image/png",
        val zerowidth: Boolean = false)