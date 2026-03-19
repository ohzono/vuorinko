package com.komakoma.vuorinko

import android.app.Application
import com.komakoma.vuorinko.db.VuorinkoDatabase
import com.komakoma.vuorinko.di.appModule
import com.komakoma.vuorinko.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class VuorinkoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@VuorinkoApplication)
            modules(appModule, sharedModule)
        }
    }
}
