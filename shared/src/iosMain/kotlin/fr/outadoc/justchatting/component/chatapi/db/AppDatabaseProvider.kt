package fr.outadoc.justchatting.db

import app.cash.sqldelight.driver.native.NativeSqliteDriver

internal object AppDatabaseProvider {

    fun get(): AppDatabase {
        return AppDatabase(
            driver = NativeSqliteDriver(
                schema = AppDatabase.Schema,
                name = "database",
            ),
        )
    }
}
