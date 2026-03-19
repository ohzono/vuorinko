package com.komakoma.vuorinko

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.komakoma.vuorinko.db.VuorinkoDatabase

fun createDriver(): SqlDriver {
    return NativeSqliteDriver(VuorinkoDatabase.Schema, "vuorinko.db")
}
