package com.witelokk.musicapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.R

@Composable
fun WelcomeScreen(navController: NavController) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painterResource(R.drawable.music),
                "",
                modifier = Modifier.size(96.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Welcome to Music App, where\n" + "you can listen to your favorite music",
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("login") },
                modifier = Modifier.requiredWidth(284.dp)
            ) {
                Text("Sign in")
            }

            Spacer(modifier = Modifier.height(4.dp))

            OutlinedButton(
                onClick = { navController.navigate("registration") },
                modifier = Modifier.requiredWidth(284.dp)
            ) {
                Text("Sign up")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Sign in with")

            Spacer(modifier = Modifier.height(4.dp))

            IconButton(onClick = {
                navController.navigate("home") { popUpTo(0) }
            }) {
                Image(
                    painterResource(R.drawable.google),
                    contentScale = ContentScale.FillWidth,
                    contentDescription = "Sign in with Google"
                )
            }
        }
    }
}