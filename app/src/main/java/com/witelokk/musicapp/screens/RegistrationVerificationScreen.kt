package com.witelokk.musicapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.R
import com.witelokk.musicapp.components.CodeField
import com.witelokk.musicapp.viewmodel.RegistrationVerificationScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data class RegistrationVerification(
    val name: String,
    val email: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationVerificationScreen(
    navController: NavController,
    registrationVerification: RegistrationVerification,
    viewModel: RegistrationVerificationScreenViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var isCodeInvalid by remember { mutableStateOf(false) }
    var isButtonEnabled by remember { mutableStateOf(false) }
    var code by remember { mutableStateOf("") }
    val codeFocusRequester = remember { FocusRequester() }
    val snackHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        codeFocusRequester.requestFocus()
    }

    LaunchedEffect(state.isCodeInvalid) {
        isCodeInvalid = state.isCodeInvalid
    }

    LaunchedEffect(state.isButtonEnabled) {
        isButtonEnabled = state.isButtonEnabled
    }

    val context = LocalContext.current
    LaunchedEffect(state.registrationFailed) {
        if (state.registrationFailed) {
            snackHostState.showSnackbar(context.getString(R.string.registration_failed_toast))
        }
    }

    LaunchedEffect(state.userAlreadyExists) {
        if (state.userAlreadyExists) {
            snackHostState.showSnackbar(
                context.getString(R.string.already_registered_toast),
                context.getString(R.string.sign_in)
            ).run {
                viewModel.singIn(registrationVerification.email, code)
            }
        }
    }

    LaunchedEffect(state.isAuthorized) {
        if (state.isAuthorized) {
            navController.navigate("home") { popUpTo(0) }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.confirm_registration)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
                    }
                })
        },
        modifier = Modifier.imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.code_prompt),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            CodeField(
                length = 4,
                onCodeChanged = {
                    code = it
                    isCodeInvalid = false

                    isButtonEnabled = code.length == 4
                },
                isCodeInvalid = isCodeInvalid,
                focusRequester = codeFocusRequester,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = {
                viewModel.registerAndLogin(
                    registrationVerification.name,
                    registrationVerification.email,
                    code
                )
            }, enabled = isButtonEnabled, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.sign_up))
            }
        }
    }
}