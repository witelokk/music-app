package com.witelokk.musicapp

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.witelokk.musicapp.data.Artist
import com.witelokk.musicapp.data.PlayerState
import com.witelokk.musicapp.data.Song
import com.witelokk.musicapp.screens.ArtistScreen
import com.witelokk.musicapp.screens.HomeScreen
import com.witelokk.musicapp.screens.LoginScreen
import com.witelokk.musicapp.screens.LoginVerificationScreen
import com.witelokk.musicapp.screens.PlaylistScreen
import com.witelokk.musicapp.screens.QueueScreen
import com.witelokk.musicapp.screens.RegistrationScreen
import com.witelokk.musicapp.screens.RegistrationVerificationScreen
import com.witelokk.musicapp.screens.SettingsScreen
import com.witelokk.musicapp.screens.WelcomeScreen
import com.witelokk.musicapp.ui.theme.MusicAppTheme
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Composable
fun App() {
    val playerState by remember {
        mutableStateOf(
            PlayerState(
                song = Song(
                    "https://avatars.yandex.net/get-music-content/14662984/ae9761c3.a.34843940-1/520x520",
                    "Die in My Heart",
                    listOf(
                        Artist(
                            "Solid Reasons", 123, "https://avatars.yandex.net/get-music-content/14728505/65f75b6e.p.23107413/400x400"
                        ), Artist(
                            "Solid Reasons", 123, "https://avatars.yandex.net/get-music-content/14728505/65f75b6e.p.23107413/400x400"
                        )
                    ),
                    2.minutes, true,
                ),
                playing = true,
                currentPosition = 20.seconds,
                previousTrackAvailable = true,
                nextTrackAvailable = true
            )
        )
    }

    val navController = rememberNavController()
    MusicAppTheme {
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
            composable("login_verification") {
                LoginVerificationScreen(navController)
            }
            composable("registration") {
                RegistrationScreen(navController)
            }
            composable("registration_verification") {
                RegistrationVerificationScreen(navController)
            }
            composable("home") {
                HomeScreen(navController, playerState)
            }
            composable("playlist") {
                PlaylistScreen(navController, playerState)
            }
            composable("artist") {
                ArtistScreen(navController, playerState)
            }
            composable("queue") {
                QueueScreen(navController, playerState)
            }
            composable("settings") {
                SettingsScreen(navController)
            }
        }
    }

}
