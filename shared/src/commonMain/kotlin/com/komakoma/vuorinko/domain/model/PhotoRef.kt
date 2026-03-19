package com.komakoma.vuorinko.domain.model

data class PhotoRef(
    val id: String,
    val albumId: String,
    val platformAssetId: String,
    val sortOrder: Int,
    val addedAt: Long,
)
