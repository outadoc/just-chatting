package fr.outadoc.justchatting.utils.logging

import kotlin.reflect.KClass

object Logger {

    var logStrategy: LogStrategy = NoopLogStrategy

    enum class Level(val tag: String) {
        Verbose("V"),
        Debug("D"),
        Info("I"),
        Warning("W"),
        Error("E"),
    }

    fun <T : Any> v(clazz: KClass<T>, content: () -> String) {
        println(Level.Verbose, clazz, content)
    }

    fun <T : Any> d(clazz: KClass<T>, content: () -> String) {
        println(Level.Debug, clazz, content)
    }

    fun <T : Any> i(clazz: KClass<T>, content: () -> String) {
        println(Level.Info, clazz, content)
    }

    fun <T : Any> w(clazz: KClass<T>, content: () -> String) {
        println(Level.Warning, clazz, content)
    }

    fun <T : Any> e(clazz: KClass<T>, throwable: Throwable?, content: () -> String) {
        println(Level.Error, clazz, content)

        if (throwable != null) {
            println(Level.Error, clazz) { throwable.stackTraceToString() }
        }
    }

    private fun <T : Any> println(level: Level, clazz: KClass<T>, content: () -> String) {
        logStrategy.println(level, clazz.simpleName, content())
    }
}
