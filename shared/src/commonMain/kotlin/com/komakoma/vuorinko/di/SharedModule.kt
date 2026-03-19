package com.komakoma.vuorinko.di

import com.komakoma.vuorinko.data.repository.AlbumRepositoryImpl
import com.komakoma.vuorinko.data.repository.AuthRepositoryImpl
import com.komakoma.vuorinko.data.repository.PhotoRepositoryImpl
import com.komakoma.vuorinko.domain.repository.AlbumRepository
import com.komakoma.vuorinko.domain.repository.AuthRepository
import com.komakoma.vuorinko.domain.repository.PhotoRepository
import com.komakoma.vuorinko.usecase.PinVerificationUseCase
import org.koin.dsl.module

val sharedModule = module {
    single<AlbumRepository> { AlbumRepositoryImpl(get()) }
    single<PhotoRepository> { PhotoRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    factory { PinVerificationUseCase(get()) }
}
