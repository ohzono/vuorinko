package com.komakoma.vuorinko.ui.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.komakoma.vuorinko.domain.model.AppConfig
import com.komakoma.vuorinko.domain.repository.AuthRepository
import com.komakoma.vuorinko.usecase.PinVerificationUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PinInputUiState(
    val pin: String = "",
    val error: String? = null,
    val isLockedOut: Boolean = false,
    val lockoutSeconds: Int = 0,
    val isAuthenticated: Boolean = false,
)

class PinInputViewModel(
    private val pinVerification: PinVerificationUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PinInputUiState())
    val uiState: StateFlow<PinInputUiState> = _uiState.asStateFlow()

    fun appendDigit(digit: Int) {
        val current = _uiState.value
        if (current.isLockedOut || current.pin.length >= AppConfig.PIN_LENGTH) return
        val newPin = current.pin + digit.toString()
        _uiState.value = current.copy(pin = newPin, error = null)
        if (newPin.length == AppConfig.PIN_LENGTH) {
            verifyPin(newPin)
        }
    }

    fun deleteDigit() {
        val current = _uiState.value
        if (current.pin.isEmpty()) return
        _uiState.value = current.copy(pin = current.pin.dropLast(1), error = null)
    }

    private fun verifyPin(pin: String) {
        viewModelScope.launch {
            when (val result = pinVerification.verify(pin)) {
                is PinVerificationUseCase.Result.Success -> {
                    _uiState.value = _uiState.value.copy(isAuthenticated = true)
                }
                is PinVerificationUseCase.Result.Failed -> {
                    _uiState.value = _uiState.value.copy(
                        pin = "",
                        error = "PINが違います（残り${result.remainingAttempts}回）"
                    )
                }
                is PinVerificationUseCase.Result.LockedOut -> {
                    startLockoutCountdown(result.remainingSeconds)
                }
            }
        }
    }

    private fun startLockoutCountdown(seconds: Int) {
        _uiState.value = _uiState.value.copy(
            pin = "",
            isLockedOut = true,
            lockoutSeconds = seconds,
            error = null
        )
        viewModelScope.launch {
            var remaining = seconds
            while (remaining > 0) {
                delay(1000)
                remaining--
                _uiState.value = _uiState.value.copy(lockoutSeconds = remaining)
            }
            _uiState.value = _uiState.value.copy(isLockedOut = false, lockoutSeconds = 0)
        }
    }
}
