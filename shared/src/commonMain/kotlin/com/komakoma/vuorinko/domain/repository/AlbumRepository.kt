package com.komakoma.vuorinko.domain.repository

import com.komakoma.vuorinko.domain.model.AlbumInfo
import com.komakoma.vuorinko.domain.model.PhotoRef
import kotlinx.coroutines.flow.Flow

interface AlbumRepository {
    fun observeAlbums(): Flow<List<AlbumInfo>>
    suspend fun getAlbum(id: String): AlbumInfo?
    suspend fun getAlbumCount(): Int
    suspend fun createAlbum(name: String): AlbumInfo
    suspend fun renameAlbum(id: String, name: String)
    suspend fun deleteAlbum(id: String)
    suspend fun reorderAlbum(id: String, newSortOrder: Int)
}

interface PhotoRepository {
    fun observePhotos(albumId: String): Flow<List<PhotoRef>>
    suspend fun getPhotoCount(albumId: String): Int
    suspend fun addPhotos(albumId: String, platformAssetIds: List<String>)
    suspend fun removePhoto(id: String)
    suspend fun reorderPhoto(id: String, newSortOrder: Int)
}
