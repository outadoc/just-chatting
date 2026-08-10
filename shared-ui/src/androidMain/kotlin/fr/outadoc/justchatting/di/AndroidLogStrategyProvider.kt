package fr.outadoc.justchatting.di

import fr.outadoc.justchatting.utils.logging.LogStrategy
import org.koin.core.context.GlobalContext

/**
 * Lets [fr.outadoc.justchatting.MainApplication] (which doesn't otherwise depend on Koin)
 * fetch the file-backed [LogStrategy] set up in [platformModule] once Koin has started.
 *
 * Deliberately a plain object rather than a [org.koin.core.component.KoinComponent]: that
 * interface isn't visible from `app-android`'s classpath since `koin-core` is an
 * `implementation`, not `api`, dependency of this module.
 */
public object AndroidLogStrategyProvider {
    public fun getLogStrategy(): LogStrategy = GlobalContext.get().get()
}
