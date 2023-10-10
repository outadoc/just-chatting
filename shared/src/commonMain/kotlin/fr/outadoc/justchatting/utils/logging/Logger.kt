package fr.outadoc.justchatting.utils.logging

import android.util.Log
import kotlin.reflect.KClass

object Logger {

    var logStrategy: LogStrategy = NoopLogStrategy

    fun <T : Any> v(clazz: KClass<T>, content: () -> String) {
        println(Log.VERBOSE, clazz, content)
    }

    fun <T : Any> d(clazz: KClass<T>, content: () -> String) {
        println(Log.DEBUG, clazz, content)
    }

    fun <T : Any> i(clazz: KClass<T>, content: () -> String) {
        println(Log.INFO, clazz, content)
    }

    fun <T : Any> w(clazz: KClass<T>, content: () -> String) {
        println(Log.WARN, clazz, content)
    }

    fun <T : Any> e(clazz: KClass<T>, throwable: Throwable?, content: () -> String) {
        println(Log.ERROR, clazz, content)

        if (throwable != null) {
            println(Log.ERROR, clazz) { throwable.stackTraceToString() }
        }
    }

    private fun <T : Any> println(level: Int, clazz: KClass<T>, content: () -> String) {
        logStrategy.println(level, clazz.simpleName, content())
    }
}
