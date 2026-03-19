package com.komakoma.vuorinko.domain.repository

interface AuthRepository {
    suspend fun isPinSet(): Boolean
    suspend fun setPin(pin: String)
    suspend fun verifyPin(pin: String): Boolean
    suspend fun getFailedAttempts(): Int
    suspend fun incrementFailedAttempts()
    suspend fun resetFailedAttempts()
    suspend fun getLastFailedAttemptTime(): Long?
    suspend fun setLastFailedAttemptTime(timeMillis: Long)
    suspend fun resetAllData()
}
