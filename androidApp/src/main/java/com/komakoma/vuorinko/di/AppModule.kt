package com.komakoma.vuorinko.di

import com.komakoma.vuorinko.createDriver
import com.komakoma.vuorinko.db.VuorinkoDatabase
import com.komakoma.vuorinko.ui.screen.auth.PinInputViewModel
import com.komakoma.vuorinko.ui.screen.child.ChildViewerViewModel
import com.komakoma.vuorinko.ui.screen.parent.AlbumListViewModel
import com.komakoma.vuorinko.ui.screen.parent.PhotoManageViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { VuorinkoDatabase(createDriver(androidContext())) }
    viewModel { AlbumListViewModel(get()) }
    viewModel { params -> PhotoManageViewModel(params.get(), get()) }
    viewModel { params -> ChildViewerViewModel(params.get(), get()) }
    viewModel { PinInputViewModel(get(), get()) }
}
