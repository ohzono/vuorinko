package com.komakoma.vuorinko.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.komakoma.vuorinko.db.VuorinkoDatabase
import com.komakoma.vuorinko.domain.model.AppConfig
import com.komakoma.vuorinko.domain.model.PhotoRef
import com.komakoma.vuorinko.domain.repository.PhotoRepository
import com.komakoma.vuorinko.usecase.currentTimeMillis
import com.komakoma.vuorinko.util.generateUuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class PhotoRepositoryImpl(
    private val database: VuorinkoDatabase,
) : PhotoRepository {

    private val photoQueries get() = database.photoQueries

    override fun observePhotos(albumId: String): Flow<List<PhotoRef>> {
        return photoQueries.selectByAlbumId(albumId)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { photos ->
                photos.map { photo ->
                    PhotoRef(
                        id = photo.id,
                        albumId = photo.albumId,
                        platformAssetId = photo.platformAssetId,
                        sortOrder = photo.sortOrder.toInt(),
                        addedAt = photo.addedAt,
                    )
                }
            }
    }

    override suspend fun getPhotoCount(albumId: String): Int = withContext(Dispatchers.Default) {
        photoQueries.countByAlbumId(albumId).executeAsOne().toInt()
    }

    override suspend fun addPhotos(albumId: String, platformAssetIds: List<String>): Unit =
        withContext(Dispatchers.Default) {
            if (platformAssetIds.isEmpty()) return@withContext

            val currentCount = photoQueries.countByAlbumId(albumId).executeAsOne().toInt()
            check(currentCount + platformAssetIds.size <= AppConfig.MAX_PHOTOS_PER_ALBUM) {
                "Cannot add more than ${AppConfig.MAX_PHOTOS_PER_ALBUM} photos per album"
            }

            val maxSortOrder = photoQueries.selectByAlbumId(albumId).executeAsList()
                .maxOfOrNull { it.sortOrder }?.toInt() ?: -1

            val now = currentTimeMillis()
            platformAssetIds.forEachIndexed { index, platformAssetId ->
                photoQueries.insert(
                    id = generateUuid(),
                    albumId = albumId,
                    platformAssetId = platformAssetId,
                    sortOrder = (maxSortOrder + 1 + index).toLong(),
                    addedAt = now,
                )
            }
        }

    override suspend fun removePhoto(id: String): Unit = withContext(Dispatchers.Default) {
        photoQueries.delete(id)
    }

    override suspend fun reorderPhoto(id: String, newSortOrder: Int): Unit = withContext(Dispatchers.Default) {
        photoQueries.updateSortOrder(
            sortOrder = newSortOrder.toLong(),
            id = id,
        )
    }
}
