package com.komakoma.vuorinko.data.repository

import com.komakoma.vuorinko.db.VuorinkoDatabase
import com.komakoma.vuorinko.domain.repository.AuthRepository
import com.komakoma.vuorinko.util.PinHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AuthRepositoryImpl(
    private val database: VuorinkoDatabase,
) : AuthRepository {

    private val appSettingsQueries get() = database.appSettingsQueries

    private companion object {
        const val KEY_PIN_HASH = "pin_hash"
        const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        const val KEY_LAST_FAILED_TIME = "last_failed_time"
    }

    override suspend fun isPinSet(): Boolean = withContext(Dispatchers.Default) {
        appSettingsQueries.get(KEY_PIN_HASH).executeAsOneOrNull() != null
    }

    override suspend fun setPin(pin: String): Unit = withContext(Dispatchers.Default) {
        val hash = PinHasher.hashPin(pin)
        appSettingsQueries.set(KEY_PIN_HASH, hash)
    }

    override suspend fun verifyPin(pin: String): Boolean = withContext(Dispatchers.Default) {
        val storedHash = appSettingsQueries.get(KEY_PIN_HASH).executeAsOneOrNull()
            ?: return@withContext false
        PinHasher.verifyPin(pin, storedHash)
    }

    override suspend fun getFailedAttempts(): Int = withContext(Dispatchers.Default) {
        appSettingsQueries.get(KEY_FAILED_ATTEMPTS).executeAsOneOrNull()?.toIntOrNull() ?: 0
    }

    override suspend fun incrementFailedAttempts(): Unit = withContext(Dispatchers.Default) {
        val current = appSettingsQueries.get(KEY_FAILED_ATTEMPTS).executeAsOneOrNull()?.toIntOrNull() ?: 0
        appSettingsQueries.set(KEY_FAILED_ATTEMPTS, (current + 1).toString())
    }

    override suspend fun resetFailedAttempts(): Unit = withContext(Dispatchers.Default) {
        appSettingsQueries.delete(KEY_FAILED_ATTEMPTS)
        appSettingsQueries.delete(KEY_LAST_FAILED_TIME)
    }

    override suspend fun getLastFailedAttemptTime(): Long? = withContext(Dispatchers.Default) {
        appSettingsQueries.get(KEY_LAST_FAILED_TIME).executeAsOneOrNull()?.toLongOrNull()
    }

    override suspend fun setLastFailedAttemptTime(timeMillis: Long): Unit = withContext(Dispatchers.Default) {
        appSettingsQueries.set(KEY_LAST_FAILED_TIME, timeMillis.toString())
    }

    override suspend fun resetAllData(): Unit = withContext(Dispatchers.Default) {
        appSettingsQueries.delete(KEY_PIN_HASH)
        appSettingsQueries.delete(KEY_FAILED_ATTEMPTS)
        appSettingsQueries.delete(KEY_LAST_FAILED_TIME)
    }
}
