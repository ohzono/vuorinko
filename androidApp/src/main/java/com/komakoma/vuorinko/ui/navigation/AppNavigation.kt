package com.komakoma.vuorinko.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.komakoma.vuorinko.domain.repository.AuthRepository
import com.komakoma.vuorinko.ui.screen.auth.PinInputScreen
import com.komakoma.vuorinko.ui.screen.auth.PinSetupScreen
import com.komakoma.vuorinko.ui.screen.onboarding.ScreenPinningGuideScreen
import com.komakoma.vuorinko.ui.screen.onboarding.WelcomeScreen
import com.komakoma.vuorinko.ui.screen.child.ChildViewerScreen
import com.komakoma.vuorinko.ui.screen.parent.AlbumListScreen
import com.komakoma.vuorinko.ui.screen.parent.PhotoManageScreen
import org.koin.compose.koinInject

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authRepository: AuthRepository = koinInject()
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        startDestination = if (authRepository.isPinSet()) "pin_input" else "welcome"
    }

    val dest = startDestination ?: return

    NavHost(navController = navController, startDestination = dest) {
        composable("welcome") {
            WelcomeScreen(onNext = {
                navController.navigate("pin_setup") {
                    popUpTo("welcome") { inclusive = true }
                }
            })
        }

        composable("pin_setup") {
            PinSetupScreen(
                onPinSet = {
                    navController.navigate("screen_pinning_guide") {
                        popUpTo("pin_setup") { inclusive = true }
                    }
                }
            )
        }

        composable("screen_pinning_guide") {
            ScreenPinningGuideScreen(
                onNext = {
                    navController.navigate("album_list") {
                        popUpTo("screen_pinning_guide") { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate("album_list") {
                        popUpTo("screen_pinning_guide") { inclusive = true }
                    }
                }
            )
        }

        composable("pin_input") {
            PinInputScreen(
                onAuthenticated = {
                    navController.navigate("album_list") {
                        popUpTo("pin_input") { inclusive = true }
                    }
                }
            )
        }

        composable("album_list") {
            AlbumListScreen(
                onAlbumClick = { albumId ->
                    navController.navigate("photo_manage/$albumId")
                },
                onChildMode = { albumId ->
                    navController.navigate("child_viewer/$albumId")
                }
            )
        }

        composable(
            "photo_manage/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: return@composable
            PhotoManageScreen(
                albumId = albumId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            "child_viewer/{albumId}",
            arguments = listOf(navArgument("albumId") { type = NavType.StringType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getString("albumId") ?: return@composable
            ChildViewerScreen(
                albumId = albumId,
                onExitChildMode = {
                    navController.navigate("album_list") {
                        popUpTo("child_viewer/$albumId") { inclusive = true }
                    }
                }
            )
        }
    }
}
