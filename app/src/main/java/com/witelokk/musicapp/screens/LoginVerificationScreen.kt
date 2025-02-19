package com.witelokk.musicapp.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun LoginVerificationScreen(navController: NavController) {
    VerificationScreen(
        navController,
        "Confirm your login",
        "Sign in"
    ) { _ -> navController.navigate("home"){
        popUpTo(0)
    }; true }
}