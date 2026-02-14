package fr.outadoc.justchatting.data.db

import app.cash.sqldelight.driver.native.NativeSqliteDriver

internal object AppDatabaseProvider {
    fun get(): AppDatabase = AppDatabase(
        driver =
        NativeSqliteDriver(
            schema = AppDatabase.Schema,
            name = "database",
        ),
    )
}
