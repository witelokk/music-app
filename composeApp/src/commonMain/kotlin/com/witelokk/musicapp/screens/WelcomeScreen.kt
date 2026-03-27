package com.witelokk.musicapp.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.viewmodel.WelcomeScreenViewModel
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.*
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WelcomeScreen(
    navController: NavController,
    viewModel: WelcomeScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isAuthorized) {
        if (state.isAuthorized) {
            navController.navigate("home") { popUpTo(0) }
        }
    }

    LaunchedEffect(state.signInFailed) {
        if (state.signInFailed) {
            snackbarHostState.showSnackbar(getString(Res.string.sign_in_failed_toast))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        AnimatedVisibility(!state.isCheckingAuthorization && !state.isAuthorized) {
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painterResource(Res.drawable.music),
                    "",
                    modifier = Modifier
                        .size(96.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            viewModel.onLogoTapped()
                        },
                    tint = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    stringResource(Res.string.welcome_message),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        scope.launch {
                            viewModel.commitServerUrl()
                            navController.navigate("login")
                        }
                    },
                    modifier = Modifier.requiredWidth(284.dp)
                ) {
                        Text(stringResource(Res.string.sign_in))
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedButton(
                    onClick = {
                        scope.launch {
                            viewModel.commitServerUrl()
                            navController.navigate(Registration())
                        }
                    },
                    modifier = Modifier.requiredWidth(284.dp)
                ) {
                        Text(stringResource(Res.string.sign_up))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(stringResource(Res.string.sign_in_with))

                Spacer(modifier = Modifier.height(4.dp))

                IconButton(onClick = {
                    viewModel.signInWithGoogle()
                }) {
                    Image(
                        painterResource(Res.drawable.google),
                        contentScale = ContentScale.FillWidth,
                        contentDescription = stringResource(Res.string.sign_in_with_google)
                    )
                }

                AnimatedVisibility(visible = state.showServerSettings) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Text(stringResource(Res.string.server_url))

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = state.serverUrlInput,
                            onValueChange = { newValue ->
                                viewModel.onServerUrlChanged(newValue)
                            },
                            singleLine = true,
                            modifier = Modifier.requiredWidth(284.dp),
                            placeholder = {
                                Text(text = stringResource(Res.string.server_url_placeholder))
                            }
                        )
                    }
                }
            }
        }
    }
}
