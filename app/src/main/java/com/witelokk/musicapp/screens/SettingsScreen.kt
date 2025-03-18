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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
        TopAppBar(title = { Text("Settings") }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
            }
        })
    }) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                Text(
                    "Account",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(16.dp)
                )
            }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Avatar("R", radius = 100f, fontSize = 32.sp, modifier = Modifier.size(100.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Name: ${state.accountName}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Email: ${state.accountEmail}")
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
                    "App",
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
                    Text("Theme", modifier = Modifier.weight(1f))

                    ExposedDropdownMenuBox(expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            when (state.theme) {
                                "system" -> "System"
                                "light" -> "Light"
                                "dark" -> "Dark"
                                else -> "System"
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
                                text = { Text("System") },
                                onClick = {
                                    viewModel.setTheme("system")
                                    themeViewModel.setTheme("system")
                                    expanded = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Light") },
                                onClick = {
                                    viewModel.setTheme("light")
                                    themeViewModel.setTheme("light")
                                    expanded = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Dark") },
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
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .height(56.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Text("Song caching", modifier = Modifier.weight(1f))
                    Switch(
                        state.songCachingEnabled ?: false,
                        onCheckedChange = {
                            viewModel.setSongCachingEnabled(it)
                        }
                    )
                }
            }
        }
    }
}