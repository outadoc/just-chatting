package com.github.andreyasadchy.xtra.util.chat

import android.util.Log
import java.io.*
import java.net.Socket
import java.util.*
import javax.net.ssl.SSLSocketFactory

private const val TAG = "LiveChatThread"

class LiveChatThread(
    private val useSSl: Boolean,
    private val loggedIn: Boolean,
    private val channelName: String,
    private val listener: OnMessageReceivedListener) : Thread() {
    private var socketIn: Socket? = null
    private lateinit var readerIn: BufferedReader
    private lateinit var writerIn: BufferedWriter
    private val hashChannelName: String = "#$channelName"
    var isActive = true

    override fun run() {

        fun handlePing(writer: BufferedWriter) {
            write("PONG :tmi.twitch.tv", writer)
            writer.flush()
        }

        do {
            try {
                connect()
                while (true) {
                    val messageIn = readerIn.readLine()!!
                    messageIn.run {
                        when {
                            contains("PRIVMSG") -> listener.onMessage(this, false)
                            contains("USERNOTICE") -> listener.onMessage(this, true)
                            contains("CLEARMSG") -> listener.onClearMessage(this)
                            contains("CLEARCHAT") -> listener.onClearChat(this)
                            contains("NOTICE") -> {
                                if (!loggedIn) {
                                    listener.onNotice(this)
                                }
                            }
                            contains("ROOMSTATE") -> listener.onRoomState(this)
                            startsWith("PING") -> handlePing(writerIn)
                        }
                    }
                }
            } catch (e: IOException) {
                Log.d(TAG, "Disconnecting from $hashChannelName")
                if (e.message != "Socket closed" && e.message != "socket is closed" && e.message != "Connection reset" && e.message != "recvfrom failed: ECONNRESET (Connection reset by peer)") {
                    listener.onCommand(message = channelName, duration = e.toString(), type = "disconnect", fullMsg = e.stackTraceToString())
                }
                close()
                sleep(1000L)
            } catch (e: Exception) {
                close()
                sleep(1000L)
            }
        } while (isActive)
    }

    private fun connect() {
        Log.d(TAG, "Connecting to Twitch IRC - SSl $useSSl")
        try {
            socketIn = (if (useSSl) SSLSocketFactory.getDefault().createSocket("irc.twitch.tv", 6697) else Socket("irc.twitch.tv", 6667)).apply {
                readerIn = BufferedReader(InputStreamReader(getInputStream()))
                writerIn = BufferedWriter(OutputStreamWriter(getOutputStream()))
            }
            write("NICK justinfan${Random().nextInt(((9999 - 1000) + 1)) + 1000}", writerIn) //random number between 1000 and 9999
            write("CAP REQ :twitch.tv/tags twitch.tv/commands", writerIn)
            write("JOIN $hashChannelName", writerIn)
            writerIn.flush()
            Log.d(TAG, "Successfully connected to - $hashChannelName")
            listener.onCommand(message = channelName, duration = null, type = "join", fullMsg = null)
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to Twitch IRC", e)
            throw e
        }
    }

    fun disconnect() {
        if (isActive) {
            val thread = Thread {
                isActive = false
                close()
            }
            thread.start()
            thread.join()
        }
    }

    private fun close() {
        try {
            socketIn?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error while closing socketIn", e)
            listener.onCommand(message = e.toString(), duration = null, type = "socket_error", fullMsg = e.stackTraceToString())
        }
    }

    @Throws(IOException::class)
    private fun write(message: String, vararg writers: BufferedWriter?) {
        writers.forEach { it?.write(message + System.getProperty("line.separator")) }
    }

    interface OnMessageReceivedListener {
        fun onMessage(message: String, userNotice: Boolean)
        fun onCommand(message: String, duration: String?, type: String?, fullMsg: String?)
        fun onClearMessage(message: String)
        fun onClearChat(message: String)
        fun onNotice(message: String)
        fun onRoomState(message: String)
        fun onUserState(message: String)
    }
}
