package com.komakoma.vuorinko.ui.screen.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.komakoma.vuorinko.domain.model.AppConfig
import com.komakoma.vuorinko.domain.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PinSetupScreen(
    onPinSet: () -> Unit,
    authRepository: AuthRepository = koinInject(),
) {
    var step by remember { mutableStateOf(0) } // 0: first input, 1: confirm
    var firstPin by remember { mutableStateOf("") }
    var currentPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            if (step == 0) "PINを設定してください" else "もう一度入力してください",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(32.dp))

        PinDots(length = currentPin.length, total = AppConfig.PIN_LENGTH)
        Spacer(Modifier.height(16.dp))

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(32.dp))
        NumberPad(
            onDigit = { digit ->
                if (currentPin.length >= AppConfig.PIN_LENGTH) return@NumberPad
                currentPin += digit.toString()
                error = null
                if (currentPin.length == AppConfig.PIN_LENGTH) {
                    if (step == 0) {
                        firstPin = currentPin
                        currentPin = ""
                        step = 1
                    } else {
                        if (currentPin == firstPin) {
                            scope.launch {
                                authRepository.setPin(currentPin)
                                onPinSet()
                            }
                        } else {
                            error = "PINが一致しません。やり直してください"
                            currentPin = ""
                            firstPin = ""
                            step = 0
                        }
                    }
                }
            },
            onDelete = {
                if (currentPin.isNotEmpty()) {
                    currentPin = currentPin.dropLast(1)
                    error = null
                }
            },
            enabled = true,
        )
    }
}
