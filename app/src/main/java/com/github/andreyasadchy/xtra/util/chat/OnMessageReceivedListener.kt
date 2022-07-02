package com.github.andreyasadchy.xtra.util.chat


interface OnMessageReceivedListener {
    fun onMessage(message: String, userNotice: Boolean)
    fun onCommand(message: String, duration: String?, type: String?, fullMsg: String?)
    fun onClearMessage(message: String)
    fun onClearChat(message: String)
    fun onNotice(message: String)
    fun onRoomState(message: String)
    fun onUserState(message: String)
}
