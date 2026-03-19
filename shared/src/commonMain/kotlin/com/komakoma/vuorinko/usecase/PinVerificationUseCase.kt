package com.komakoma.vuorinko.usecase

import com.komakoma.vuorinko.domain.model.AppConfig
import com.komakoma.vuorinko.domain.repository.AuthRepository

class PinVerificationUseCase(
    private val authRepository: AuthRepository,
    private val currentTimeProvider: () -> Long = { currentTimeMillis() },
) {
    sealed class Result {
        data object Success : Result()
        data class Failed(val remainingAttempts: Int) : Result()
        data class LockedOut(val remainingSeconds: Int) : Result()
    }

    suspend fun verify(pin: String): Result {
        val lockoutRemaining = getLockoutRemainingSeconds()
        if (lockoutRemaining > 0) {
            return Result.LockedOut(lockoutRemaining)
        }

        val isValid = authRepository.verifyPin(pin)
        if (isValid) {
            authRepository.resetFailedAttempts()
            return Result.Success
        }

        authRepository.incrementFailedAttempts()
        authRepository.setLastFailedAttemptTime(currentTimeProvider())
        val failedAttempts = authRepository.getFailedAttempts()
        val remaining = AppConfig.MAX_PIN_ATTEMPTS - failedAttempts

        return if (remaining <= 0) {
            Result.LockedOut(AppConfig.LOCKOUT_DURATION_SECONDS)
        } else {
            Result.Failed(remaining)
        }
    }

    suspend fun getLockoutRemainingSeconds(): Int {
        val failedAttempts = authRepository.getFailedAttempts()
        if (failedAttempts < AppConfig.MAX_PIN_ATTEMPTS) return 0

        val lastFailedTime = authRepository.getLastFailedAttemptTime() ?: return 0
        val elapsed = (currentTimeProvider() - lastFailedTime) / 1000
        val remaining = AppConfig.LOCKOUT_DURATION_SECONDS - elapsed.toInt()
        if (remaining <= 0) {
            authRepository.resetFailedAttempts()
            return 0
        }
        return remaining
    }
}

internal expect fun currentTimeMillis(): Long
