package com.komakoma.vuorinko

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.komakoma.vuorinko.db.VuorinkoDatabase

fun createDriver(context: Context): SqlDriver {
    return AndroidSqliteDriver(VuorinkoDatabase.Schema, context, "vuorinko.db")
}
