package com.komakoma.vuorinko.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.komakoma.vuorinko.db.VuorinkoDatabase
import com.komakoma.vuorinko.domain.model.AlbumInfo
import com.komakoma.vuorinko.domain.model.AppConfig
import com.komakoma.vuorinko.domain.repository.AlbumRepository
import com.komakoma.vuorinko.usecase.currentTimeMillis
import com.komakoma.vuorinko.util.generateUuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

internal class AlbumRepositoryImpl(
    private val database: VuorinkoDatabase,
) : AlbumRepository {

    private val albumQueries get() = database.albumQueries
    private val photoQueries get() = database.photoQueries

    override fun observeAlbums(): Flow<List<AlbumInfo>> {
        val albumsFlow = albumQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)

        val photosFlow = photoQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)

        return combine(albumsFlow, photosFlow) { albums, photos ->
            val countByAlbumId = photos.groupBy { it.albumId }.mapValues { it.value.size }
            albums.map { album ->
                AlbumInfo(
                    id = album.id,
                    name = album.name,
                    sortOrder = album.sortOrder.toInt(),
                    photoCount = countByAlbumId[album.id] ?: 0,
                    createdAt = album.createdAt,
                    updatedAt = album.updatedAt,
                )
            }
        }
    }

    override suspend fun getAlbum(id: String): AlbumInfo? = withContext(Dispatchers.Default) {
        val album = albumQueries.selectById(id).executeAsOneOrNull() ?: return@withContext null
        val photoCount = photoQueries.countByAlbumId(album.id).executeAsOne().toInt()
        AlbumInfo(
            id = album.id,
            name = album.name,
            sortOrder = album.sortOrder.toInt(),
            photoCount = photoCount,
            createdAt = album.createdAt,
            updatedAt = album.updatedAt,
        )
    }

    override suspend fun getAlbumCount(): Int = withContext(Dispatchers.Default) {
        albumQueries.count().executeAsOne().toInt()
    }

    override suspend fun createAlbum(name: String): AlbumInfo = withContext(Dispatchers.Default) {
        val trimmedName = name.trim()
        check(trimmedName.isNotEmpty()) { "Album name must not be blank" }
        check(trimmedName.length <= AppConfig.MAX_ALBUM_NAME_LENGTH) {
            "Album name must not exceed ${AppConfig.MAX_ALBUM_NAME_LENGTH} characters"
        }

        val currentCount = albumQueries.count().executeAsOne().toInt()
        check(currentCount < AppConfig.MAX_ALBUMS) {
            "Cannot create more than ${AppConfig.MAX_ALBUMS} albums"
        }

        val maxSortOrder = albumQueries.selectAll().executeAsList()
            .maxOfOrNull { it.sortOrder }?.toInt() ?: -1
        val newSortOrder = maxSortOrder + 1

        val now = currentTimeMillis()
        val id = generateUuid()

        albumQueries.insert(
            id = id,
            name = trimmedName,
            sortOrder = newSortOrder.toLong(),
            createdAt = now,
            updatedAt = now,
        )

        AlbumInfo(
            id = id,
            name = trimmedName,
            sortOrder = newSortOrder,
            photoCount = 0,
            createdAt = now,
            updatedAt = now,
        )
    }

    override suspend fun renameAlbum(id: String, name: String): Unit = withContext(Dispatchers.Default) {
        val trimmedName = name.trim()
        check(trimmedName.isNotEmpty()) { "Album name must not be blank" }
        check(trimmedName.length <= AppConfig.MAX_ALBUM_NAME_LENGTH) {
            "Album name must not exceed ${AppConfig.MAX_ALBUM_NAME_LENGTH} characters"
        }

        albumQueries.updateName(
            name = trimmedName,
            updatedAt = currentTimeMillis(),
            id = id,
        )
    }

    override suspend fun deleteAlbum(id: String): Unit = withContext(Dispatchers.Default) {
        albumQueries.delete(id)
    }

    override suspend fun reorderAlbum(id: String, newSortOrder: Int): Unit = withContext(Dispatchers.Default) {
        albumQueries.updateSortOrder(
            sortOrder = newSortOrder.toLong(),
            updatedAt = currentTimeMillis(),
            id = id,
        )
    }
}
