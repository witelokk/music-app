package com.witelokk.musicapp

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import com.witelokk.musicapp.auth.AuthStore
import com.witelokk.musicapp.screens.ArtistScreen
import com.witelokk.musicapp.screens.ArtistScreenRoute
import com.witelokk.musicapp.screens.FavoritesScreen
import com.witelokk.musicapp.screens.HomeScreen
import com.witelokk.musicapp.screens.LoginScreen
import com.witelokk.musicapp.screens.LoginVerification
import com.witelokk.musicapp.screens.LoginVerificationScreen
import com.witelokk.musicapp.screens.PlaylistScreen
import com.witelokk.musicapp.screens.PlaylistScreenRoute
import com.witelokk.musicapp.screens.QueueScreen
import com.witelokk.musicapp.screens.ReleaseScreen
import com.witelokk.musicapp.screens.ReleaseScreenRoute
import com.witelokk.musicapp.screens.Registration
import com.witelokk.musicapp.screens.RegistrationScreen
import com.witelokk.musicapp.screens.RegistrationVerification
import com.witelokk.musicapp.screens.RegistrationVerificationScreen
import com.witelokk.musicapp.screens.SettingsScreen
import com.witelokk.musicapp.screens.WelcomeScreen
import com.witelokk.musicapp.ui.theme.MusicAppTheme
import com.witelokk.musicapp.viewmodel.ThemeViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalCoilApi::class)
@Composable
fun App(
    themeViewModel: ThemeViewModel = koinViewModel(),
    imageLoader: ImageLoader = koinInject(),
    authStore: AuthStore = koinInject(),
    offlineLibrarySync: OfflineLibrarySync = koinInject(),
) {
    val theme by themeViewModel.theme.collectAsState()
    val dynamicColor by themeViewModel.dynamicColor.collectAsState()
    val authState by authStore.state.collectAsState()

    setSingletonImageLoaderFactory { imageLoader }

    val navController = rememberNavController()
    var wasAuthorized by remember { mutableStateOf(authState.isAuthorized) }

    LaunchedEffect(authState.isAuthorized) {
        if (wasAuthorized && !authState.isAuthorized) {
            navController.navigate("welcome") {
                popUpTo(0)
            }
        }
        if (authState.isAuthorized) {
            offlineLibrarySync.sync()
        }
        wasAuthorized = authState.isAuthorized
    }

    MusicAppTheme(
        darkTheme = when (theme) {
            "light" -> false
            "dark" -> true
            else -> {
                isSystemInDarkTheme()
            }
        },
        dynamicColor = dynamicColor
    ) {
        NavHost(navController, startDestination = "welcome", enterTransition = {
            EnterTransition.None
        }, exitTransition = {
            ExitTransition.None
        }, modifier = Modifier.fillMaxSize()) {
            composable("welcome") {
                WelcomeScreen(navController)
            }
            composable("login") {
                LoginScreen(navController)
            }
            composable<LoginVerification> {
                val loginVerification = it.toRoute<LoginVerification>()
                LoginVerificationScreen(navController, loginVerification)
            }
            composable<Registration> {
                val registration = it.toRoute<Registration>()
                RegistrationScreen(navController, registration)
            }
            composable<RegistrationVerification> {
                val registrationVerification = it.toRoute<RegistrationVerification>()
                RegistrationVerificationScreen(navController, registrationVerification)
            }
            composable("home") {
                HomeScreen(navController)
            }
            composable<PlaylistScreenRoute> {
                val route = it.toRoute<PlaylistScreenRoute>()
                PlaylistScreen(navController, route)
            }
            composable<ReleaseScreenRoute> {
                val route = it.toRoute<ReleaseScreenRoute>()
                ReleaseScreen(navController, route)
            }
            composable("favorites") {
                FavoritesScreen(navController)
            }
            composable<ArtistScreenRoute> {
                val route = it.toRoute<ArtistScreenRoute>()
                ArtistScreen(navController, route)
            }
            composable("queue") {
                QueueScreen(navController)
            }
            composable("settings") {
                SettingsScreen(navController)
            }
        }
    }
}
