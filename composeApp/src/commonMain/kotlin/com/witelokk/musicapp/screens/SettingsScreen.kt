package com.witelokk.musicapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.witelokk.musicapp.components.Avatar
import com.witelokk.musicapp.supportsDynamicColors
import com.witelokk.musicapp.viewmodel.SettingsScreenState
import com.witelokk.musicapp.viewmodel.SettingsScreenViewModel
import musicapp.composeapp.generated.resources.Res
import musicapp.composeapp.generated.resources.cancel_action
import musicapp.composeapp.generated.resources.clear_action
import musicapp.composeapp.generated.resources.navigate_back_content_description
import musicapp.composeapp.generated.resources.settings_app_data_clear_dialog_text
import musicapp.composeapp.generated.resources.settings_app_data_clear_dialog_title
import musicapp.composeapp.generated.resources.settings_app_data_section_title
import musicapp.composeapp.generated.resources.settings_appearance_section_title
import musicapp.composeapp.generated.resources.settings_auto_download_favorites_description
import musicapp.composeapp.generated.resources.settings_auto_download_favorites_label
import musicapp.composeapp.generated.resources.settings_auto_download_playlists_description
import musicapp.composeapp.generated.resources.settings_auto_download_playlists_label
import musicapp.composeapp.generated.resources.settings_clear_app_data_label
import musicapp.composeapp.generated.resources.settings_download_only_wifi_description
import musicapp.composeapp.generated.resources.settings_download_only_wifi_label
import musicapp.composeapp.generated.resources.settings_dynamic_colors_description
import musicapp.composeapp.generated.resources.settings_dynamic_colors_label
import musicapp.composeapp.generated.resources.settings_email_label
import musicapp.composeapp.generated.resources.settings_language_description
import musicapp.composeapp.generated.resources.settings_language_label
import musicapp.composeapp.generated.resources.settings_logout_dialog_text
import musicapp.composeapp.generated.resources.settings_logout_dialog_title
import musicapp.composeapp.generated.resources.settings_logout_action
import musicapp.composeapp.generated.resources.settings_name_label
import musicapp.composeapp.generated.resources.settings_offline_downloads_section_title
import musicapp.composeapp.generated.resources.settings_screen_title
import musicapp.composeapp.generated.resources.settings_theme_dark_option
import musicapp.composeapp.generated.resources.settings_theme_label
import musicapp.composeapp.generated.resources.settings_theme_light_option
import musicapp.composeapp.generated.resources.settings_theme_system_option
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsScreenViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) {
            navController.navigate("welcome") {
                popUpTo(0)
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmSettingsDialog(
            title = stringResource(Res.string.settings_logout_dialog_title),
            text = stringResource(Res.string.settings_logout_dialog_text),
            confirmText = stringResource(Res.string.settings_logout_action),
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showClearDataDialog) {
        ConfirmSettingsDialog(
            title = stringResource(Res.string.settings_app_data_clear_dialog_title),
            text = stringResource(Res.string.settings_app_data_clear_dialog_text),
            confirmText = stringResource(Res.string.clear_action),
            onConfirm = {
                showClearDataDialog = false
                viewModel.clearAppData()
            },
            onDismiss = { showClearDataDialog = false }
        )
    }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text(stringResource(Res.string.settings_screen_title), overflow = TextOverflow.Ellipsis, maxLines = 1)
        }, navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(Res.string.navigate_back_content_description))
            }
        })
    }) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AccountInfo(state)
                    SettingsActionRow(
                        title = stringResource(Res.string.settings_logout_action),
                        contentColor = MaterialTheme.colorScheme.error,
                        onClick = { showLogoutDialog = true }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(Res.string.settings_appearance_section_title)) {
                    SettingsRow(title = stringResource(Res.string.settings_theme_label)) {
                        ThemeDropdown(
                            theme = state.theme,
                            onThemeSelected = { theme ->
                                viewModel.setTheme(theme)
                            }
                        )
                    }
                    if (supportsDynamicColors()) {
                        SettingsSwitchRow(
                            title = stringResource(Res.string.settings_dynamic_colors_label),
                            description = stringResource(Res.string.settings_dynamic_colors_description),
                            checked = state.useDynamicColors,
                            onCheckedChange = { enabled ->
                                viewModel.setUseDynamicColors(enabled)
                            }
                        )
                    }
                }
            }

            item {
                SettingsSection(title = stringResource(Res.string.settings_language_label)) {
                    SettingsActionRow(
                        title = stringResource(Res.string.settings_language_label),
                        description = stringResource(Res.string.settings_language_description),
                        showChevron = true,
                        onClick = { viewModel.openLanguageSettings() }
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(Res.string.settings_offline_downloads_section_title)) {
                    SettingsSwitchRow(
                        title = stringResource(Res.string.settings_auto_download_favorites_label),
                        description = stringResource(Res.string.settings_auto_download_favorites_description),
                        checked = state.autoDownloadFavorites,
                        onCheckedChange = viewModel::setAutoDownloadFavorites
                    )
                    SettingsSwitchRow(
                        title = stringResource(Res.string.settings_auto_download_playlists_label),
                        description = stringResource(Res.string.settings_auto_download_playlists_description),
                        checked = state.autoDownloadPlaylists,
                        onCheckedChange = viewModel::setAutoDownloadPlaylists
                    )
                    SettingsSwitchRow(
                        title = stringResource(Res.string.settings_download_only_wifi_label),
                        description = stringResource(Res.string.settings_download_only_wifi_description),
                        checked = state.downloadOnlyOnWifi,
                        onCheckedChange = viewModel::setDownloadOnlyOnWifi
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(Res.string.settings_app_data_section_title)) {
                    SettingsActionRow(
                        title = stringResource(Res.string.settings_clear_app_data_label),
                        contentColor = MaterialTheme.colorScheme.error,
                        onClick = { showClearDataDialog = true }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
        )
        Column(content = content)
    }
}

@Composable
private fun AccountInfo(state: SettingsScreenState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Avatar(
            state.accountName?.take(1).orEmpty(),
            radius = 100f,
            fontSize = 32.sp,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.width(24.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(Res.string.settings_name_label, state.accountName ?: ""),
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(Res.string.settings_email_label, state.accountEmail ?: ""),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    description: String? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    showChevron: Boolean = false,
    onClick: () -> Unit,
) {
    SettingsRow(
        title = title,
        description = description,
        contentColor = contentColor,
        trailing = {
            if (showChevron) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = contentColor)
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SettingsRow(
    title: String,
    description: String? = null,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = contentColor)
            if (description != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        trailing()
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsRow(
        title = title,
        description = description,
        trailing = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeDropdown(
    theme: String?,
    onThemeSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = when (theme) {
                "system" -> stringResource(Res.string.settings_theme_system_option)
                "light" -> stringResource(Res.string.settings_theme_light_option)
                "dark" -> stringResource(Res.string.settings_theme_dark_option)
                else -> stringResource(Res.string.settings_theme_system_option)
            },
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .width(170.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(expanded, { expanded = false }) {
            ThemeDropdownItem(
                text = stringResource(Res.string.settings_theme_system_option),
                value = "system",
                onThemeSelected = onThemeSelected,
                onDismiss = { expanded = false }
            )
            ThemeDropdownItem(
                text = stringResource(Res.string.settings_theme_light_option),
                value = "light",
                onThemeSelected = onThemeSelected,
                onDismiss = { expanded = false }
            )
            ThemeDropdownItem(
                text = stringResource(Res.string.settings_theme_dark_option),
                value = "dark",
                onThemeSelected = onThemeSelected,
                onDismiss = { expanded = false }
            )
        }
    }
}

@Composable
private fun ThemeDropdownItem(
    text: String,
    value: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenuItem(
        text = { Text(text) },
        onClick = {
            onThemeSelected(value)
            onDismiss()
        },
    )
}

@Composable
private fun ConfirmSettingsDialog(
    title: String,
    text: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel_action))
            }
        }
    )
}
