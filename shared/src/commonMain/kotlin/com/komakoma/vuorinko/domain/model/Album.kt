package com.komakoma.vuorinko.domain.model

data class AlbumInfo(
    val id: String,
    val name: String,
    val sortOrder: Int,
    val photoCount: Int,
    val createdAt: Long,
    val updatedAt: Long,
)
