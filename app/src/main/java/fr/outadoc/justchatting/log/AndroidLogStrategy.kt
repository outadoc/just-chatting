package fr.outadoc.justchatting.log

import android.util.Log

object AndroidLogStrategy : LogStrategy {
    override fun println(level: Int, tag: String?, content: String) {
        Log.println(level, tag, content)
    }
}
