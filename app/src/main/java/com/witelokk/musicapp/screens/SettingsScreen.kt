package com.witelokk.musicapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.witelokk.musicapp.R
import com.witelokk.musicapp.components.Avatar
import com.witelokk.musicapp.viewmodel.SettingsScreenViewModel
import com.witelokk.musicapp.viewmodel.ThemeViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsScreenViewModel = koinViewModel(),
    themeViewModel: ThemeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            navController.navigate("welcome") {
                popUpTo(0)
            }
        }
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(R.string.settings)) }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.back))
            }
        })
    }) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                Text(
                    stringResource(R.string.account),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Avatar(
                        if (state.accountName.isNullOrEmpty()) "" else state.accountName!!.substring(
                            0,
                            1
                        ), radius = 100f, fontSize = 32.sp, modifier = Modifier.size(100.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.name_label, state.accountName ?: ""))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.email_label, state.accountEmail ?: ""))
                    }
                    IconButton(onClick = {
                        viewModel.logout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null)
                    }
                }
            }

            item {
                Text(
                    stringResource(R.string.app),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }
            item {
                var expanded by remember { mutableStateOf(false) }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(66.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text(stringResource(R.string.theme), modifier = Modifier.weight(1f))

                    ExposedDropdownMenuBox(expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            when (state.theme) {
                                "system" -> stringResource(R.string.system)
                                "light" -> stringResource(R.string.light)
                                "dark" -> stringResource(R.string.dark)
                                else -> stringResource(R.string.system)
                            },
                            onValueChange = {},
                            readOnly = true,
                            singleLine = true,
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .width(150.dp),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors()
                        )
                        ExposedDropdownMenu(expanded, { expanded = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.system)) },
                                onClick = {
                                    viewModel.setTheme("system")
                                    themeViewModel.setTheme("system")
                                    expanded = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.light)) },
                                onClick = {
                                    viewModel.setTheme("light")
                                    themeViewModel.setTheme("light")
                                    expanded = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.dark)) },
                                onClick = {
                                    viewModel.setTheme("dark")
                                    themeViewModel.setTheme("dark")
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}