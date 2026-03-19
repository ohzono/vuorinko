package com.komakoma.vuorinko.util

internal actual fun generateUuid(): String = java.util.UUID.randomUUID().toString()
