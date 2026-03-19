package com.komakoma.vuorinko.ui.screen.child

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil3.compose.AsyncImage
import com.komakoma.vuorinko.ui.screen.auth.PinDots
import com.komakoma.vuorinko.ui.screen.auth.NumberPad
import com.komakoma.vuorinko.domain.model.AppConfig
import com.komakoma.vuorinko.usecase.PinVerificationUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChildViewerScreen(
    albumId: String,
    onExitChildMode: () -> Unit,
    viewModel: ChildViewerViewModel = koinViewModel { parametersOf(albumId) },
) {
    val photos by viewModel.photos.collectAsState()
    var showPinDialog by remember { mutableStateOf(false) }
    var tapCount by remember { mutableIntStateOf(0) }
    val view = LocalView.current

    // Immersive mode
    DisposableEffect(Unit) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowInsetsControllerCompat(window, view)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        WindowCompat.setDecorFitsSystemWindows(window, false)

        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { /* ignore zoom */ },
                    onLongPress = { /* ignore */ },
                    onTap = { offset ->
                        // Triple tap on top area to show PIN dialog
                        if (offset.y < size.height * 0.15f) {
                            tapCount++
                            if (tapCount >= 3) {
                                showPinDialog = true
                                tapCount = 0
                            }
                        } else {
                            tapCount = 0
                        }
                    }
                )
            }
    ) {
        if (photos.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { photos.size })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
            ) { page ->
                AsyncImage(
                    model = photos[page].platformAssetId,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
        } else {
            Text(
                "写真がありません",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }

    if (showPinDialog) {
        ExitPinDialog(
            onDismiss = { showPinDialog = false },
            onAuthenticated = {
                showPinDialog = false
                onExitChildMode()
            }
        )
    }
}

@Composable
private fun ExitPinDialog(
    onDismiss: () -> Unit,
    onAuthenticated: () -> Unit,
) {
    val pinVerification: PinVerificationUseCase = koinInject()
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("親モードに戻る") },
        text = {
            Box(contentAlignment = Alignment.Center) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    PinDots(length = pin.length, total = AppConfig.PIN_LENGTH)
                    if (error != null) {
                        Text(error!!, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                    }
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.fillMaxSize().then(Modifier.fillMaxSize(0f))
                    )
                    NumberPad(
                        onDigit = { digit ->
                            if (pin.length >= AppConfig.PIN_LENGTH) return@NumberPad
                            pin += digit.toString()
                            error = null
                            if (pin.length == AppConfig.PIN_LENGTH) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    when (val result = pinVerification.verify(pin)) {
                                        is PinVerificationUseCase.Result.Success -> onAuthenticated()
                                        is PinVerificationUseCase.Result.Failed -> {
                                            pin = ""
                                            error = "PINが違います（残り${result.remainingAttempts}回）"
                                        }
                                        is PinVerificationUseCase.Result.LockedOut -> {
                                            pin = ""
                                            error = "ロック中（${result.remainingSeconds}秒）"
                                        }
                                    }
                                }
                            }
                        },
                        onDelete = {
                            if (pin.isNotEmpty()) {
                                pin = pin.dropLast(1)
                                error = null
                            }
                        },
                        enabled = true,
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text("キャンセル") }
        }
    )
}
