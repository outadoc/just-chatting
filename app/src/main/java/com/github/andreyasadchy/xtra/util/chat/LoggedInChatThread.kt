package com.github.andreyasadchy.xtra.util.chat

import android.util.Log
import com.github.andreyasadchy.xtra.ui.view.chat.ChatView
import java.io.*
import java.net.Socket
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.net.ssl.SSLSocketFactory

private const val TAG = "LoggedInChatThread"

class LoggedInChatThread(
    private val useSSl: Boolean,
    private val userLogin: String?,
    private val userToken: String?,
    private val channelName: String,
    private val listener: OnMessageReceivedListener) : Thread(), ChatView.MessageSenderCallback {
    private var socketOut: Socket? = null
    private lateinit var readerOut: BufferedReader
    private lateinit var writerOut: BufferedWriter
    private val hashChannelName: String = "#$channelName"
    private val messageSenderExecutor: Executor = Executors.newSingleThreadExecutor()
    private var isActive = true

    override fun run() {

        fun handlePing(writer: BufferedWriter) {
            write("PONG :tmi.twitch.tv", writer)
            writer.flush()
        }

        do {
            try {
                connect()
                while (true) {
                    val messageOut = readerOut.readLine()!!
                    messageOut.run {
                        when {
                            contains("PRIVMSG") -> {}
                            contains("USERNOTICE") -> {}
                            contains("CLEARMSG") -> {}
                            contains("CLEARCHAT") -> {}
                            contains("NOTICE") -> listener.onNotice(this)
                            contains("ROOMSTATE") -> {}
                            contains("USERSTATE") -> listener.onUserState(this)
                            startsWith("PING") -> handlePing(writerOut)
                        }
                    }
                }
            } catch (e: IOException) {
                Log.d(TAG, "Disconnecting from $hashChannelName")
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
            socketOut = (if (useSSl) SSLSocketFactory.getDefault().createSocket("irc.twitch.tv", 6697) else Socket("irc.twitch.tv", 6667)).apply {
                readerOut = BufferedReader(InputStreamReader(getInputStream()))
                writerOut = BufferedWriter(OutputStreamWriter(getOutputStream()))
                write("PASS oauth:$userToken", writerOut)
                write("NICK $userLogin", writerOut)
            }
            write("CAP REQ :twitch.tv/tags twitch.tv/commands", writerOut)
            write("JOIN $hashChannelName", writerOut)
            writerOut.flush()
            Log.d(TAG, "Successfully connected to - $hashChannelName")
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
            socketOut?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error while closing socketOut", e)
            listener.onCommand(message = e.toString(), duration = null, type = "socket_error", fullMsg = e.stackTraceToString())
        }
    }

    @Throws(IOException::class)
    private fun write(message: String, vararg writers: BufferedWriter?) {
        writers.forEach { it?.write(message + System.getProperty("line.separator")) }
    }

    override fun send(message: CharSequence) {
        messageSenderExecutor.execute {
            try {
                write("PRIVMSG $hashChannelName :$message", writerOut)
                writerOut.flush()
                Log.d(TAG, "Sent message to $hashChannelName: $message")
            } catch (e: IOException) {
                Log.e(TAG, "Error sending message", e)
                listener.onCommand(message = e.toString(), duration = null, type = "send_msg_error", fullMsg = e.stackTraceToString())
            }
        }
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
