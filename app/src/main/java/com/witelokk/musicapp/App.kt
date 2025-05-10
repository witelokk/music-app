package com.witelokk.musicapp

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.witelokk.musicapp.screens.ArtistScreen
import com.witelokk.musicapp.screens.ArtistScreenRoute
import com.witelokk.musicapp.screens.HomeScreen
import com.witelokk.musicapp.screens.LoginScreen
import com.witelokk.musicapp.screens.LoginVerification
import com.witelokk.musicapp.screens.LoginVerificationScreen
import com.witelokk.musicapp.screens.PlaylistScreen
import com.witelokk.musicapp.screens.QueueScreen
import com.witelokk.musicapp.screens.Registration
import com.witelokk.musicapp.screens.RegistrationScreen
import com.witelokk.musicapp.screens.RegistrationVerification
import com.witelokk.musicapp.screens.RegistrationVerificationScreen
import com.witelokk.musicapp.screens.SettingsScreen
import com.witelokk.musicapp.screens.WelcomeScreen
import com.witelokk.musicapp.ui.theme.MusicAppTheme
import com.witelokk.musicapp.viewmodel.ThemeViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun App(themeViewModel: ThemeViewModel = koinViewModel()) {
    val theme by themeViewModel.theme.collectAsState()
    val systemUiController = rememberSystemUiController()
    val isSystemDarkTheme = isSystemInDarkTheme()

    LaunchedEffect(theme, isSystemDarkTheme) {
        when(theme) {
            "light" -> systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = true
            )
            "dark" -> systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = false
            )
            else -> systemUiController.setSystemBarsColor(
                color = Color.Transparent,
                darkIcons = !isSystemDarkTheme
            )
        }
    }

    val navController = rememberNavController()
    MusicAppTheme(
        darkTheme = when (theme) {
            "light" -> false
            "dark" -> true
            else -> {
                isSystemInDarkTheme()
            }
        }
    ) {
        NavHost(navController, startDestination = "welcome", enterTransition = {
            EnterTransition.None
        }, exitTransition = {
            ExitTransition.None
        }) {
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
            composable("playlist") {
                PlaylistScreen(navController)
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
