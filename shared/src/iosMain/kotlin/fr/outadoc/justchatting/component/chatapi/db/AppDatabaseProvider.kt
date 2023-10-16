package fr.outadoc.justchatting.component.chatapi.db

import app.cash.sqldelight.driver.native.NativeSqliteDriver

object AppDatabaseProvider {

    fun get(): AppDatabase {
        return AppDatabase(
            driver = NativeSqliteDriver(
                schema = AppDatabase.Schema,
                name = "database"
            )
        )
    }
}
