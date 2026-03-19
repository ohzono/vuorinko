package com.komakoma.vuorinko.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.komakoma.vuorinko.domain.model.AppConfig
import org.koin.androidx.compose.koinViewModel

@Composable
fun PinInputScreen(
    onAuthenticated: () -> Unit,
    viewModel: PinInputViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthenticated()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("PINを入力", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        PinDots(length = uiState.pin.length, total = AppConfig.PIN_LENGTH)
        Spacer(Modifier.height(16.dp))

        if (uiState.isLockedOut) {
            Text(
                "ロック中...${uiState.lockoutSeconds}秒後に再試行",
                color = MaterialTheme.colorScheme.error,
            )
        } else if (uiState.error != null) {
            Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(32.dp))
        NumberPad(
            onDigit = viewModel::appendDigit,
            onDelete = viewModel::deleteDigit,
            enabled = !uiState.isLockedOut,
        )
    }
}

@Composable
fun PinDots(length: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < length) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    )
            )
        }
    }
}

@Composable
fun NumberPad(onDigit: (Int) -> Unit, onDelete: () -> Unit, enabled: Boolean) {
    val rows = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
        listOf(-1, 0, -2),
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                row.forEach { key ->
                    when (key) {
                        -1 -> Spacer(Modifier.size(72.dp))
                        -2 -> Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .clickable(enabled = enabled) { onDelete() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("⌫", style = MaterialTheme.typography.headlineSmall)
                        }
                        else -> Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(enabled = enabled) { onDigit(key) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                key.toString(),
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}
