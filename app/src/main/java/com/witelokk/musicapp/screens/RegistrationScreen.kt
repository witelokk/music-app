package com.witelokk.musicapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.witelokk.musicapp.R
import com.witelokk.musicapp.viewmodel.RegistrationScreenViewModel
import kotlinx.serialization.Serializable
import org.koin.androidx.compose.koinViewModel

@Serializable
data class Registration(
    val email: String? = null,
    val code: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    navController: NavController,
    registration: Registration,
    viewModel: RegistrationScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf(registration.email ?: "") }
    var isEmailFieldError = false
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(state.isVerificationCodeSent) {
        if (state.isVerificationCodeSent) {
            navController.navigate(RegistrationVerification(name, email))
        }
        viewModel.clearState()
    }

    LaunchedEffect(state.isEmailInvalid) {
        if (state.isEmailInvalid) {
            isEmailFieldError = true
        }
    }

    val context = LocalContext.current
    LaunchedEffect(state.verificationCodeRequestFailed) {
        if (state.verificationCodeRequestFailed) {
            snackbarHostState.showSnackbar(context.getString(R.string.verification_request_error_toast))
        }
    }

    LaunchedEffect(state.isCodeInvalid) {
        if (state.isCodeInvalid) {
            snackbarHostState.showSnackbar(context.getString(R.string.invalid_code_toast))
            viewModel.sendVerificationCode(email)
            viewModel.clearState()
            navController.navigate(RegistrationVerification(name, email))
        }
    }

    LaunchedEffect(state.registrationFailed) {
        if (state.registrationFailed) {
            snackbarHostState.showSnackbar(context.getString(R.string.registration_failed_toast))
            viewModel.clearState()
        }
    }

    LaunchedEffect(state.isAuthorized) {
        if (state.isAuthorized) {
            navController.navigate("home") {
                popUpTo(0)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.sign_up_title)) }, navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
                }
            })
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                name,
                { name = it },
                placeholder = { Text(stringResource(R.string.name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                email,
                { email = it },
                placeholder = { Text(stringResource(R.string.email)) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                enabled = registration.email == null,
                isError = isEmailFieldError,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (registration.code != null && !state.isCodeInvalid) {
                        viewModel.registerAndSignIn(name, email, registration.code)
                    } else {
                        viewModel.sendVerificationCode(email)
                        viewModel.clearState()
                        navController.navigate(RegistrationVerification(name, email))
                    }
                },
                enabled = name.isNotEmpty() && email.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.register)) }

            TextButton(
                onClick = {
                    navController.navigate(RegistrationVerification(name, email))
                },
                enabled = name.isNotEmpty() && email.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.i_already_have_a_code))
            }

        }
    }
}