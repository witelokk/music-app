package com.witelokk.musicapp.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun RegistrationVerificationScreen(navController: NavController) {
    VerificationScreen(
        navController,
        "Verify your email",
        "Confirm"
    ) { _ -> navController.navigate("home"){
        popUpTo(0)
    }; true }
}