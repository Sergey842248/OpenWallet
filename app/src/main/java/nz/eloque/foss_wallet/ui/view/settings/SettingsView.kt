package nz.eloque.foss_wallet.ui.view.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.persistence.AccentColor
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.persistence.MembershipCardImageDisplay
import nz.eloque.foss_wallet.persistence.ThemeMode
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import nz.eloque.foss_wallet.model.Pass // Import Pass
import nz.eloque.foss_wallet.model.PassType // Import PassType
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.toArgb
import android.graphics.Color as AndroidColor // Alias to avoid conflict with Compose Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Slider
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color as ComposeColor // Alias to avoid conflict with Android Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    settingsViewModel: SettingsViewModel,
) {
    val coroutineScope = rememberCoroutineScope()
    val settings = settingsViewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection(
            heading = stringResource(R.string.settings_section_appearance),
        ) {
            Text(
                text = stringResource(R.string.theme_mode),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            var expandedTheme by remember { mutableStateOf(false) }
            val selectedThemeOption = settings.value.themeMode

            ExposedDropdownMenuBox(
                expanded = expandedTheme,
                onExpandedChange = { expandedTheme = !expandedTheme },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = when (selectedThemeOption) {
                        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                        ThemeMode.DARK -> stringResource(R.string.theme_dark)
                        ThemeMode.BLACK -> stringResource(R.string.theme_black)
                    },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTheme) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedTheme,
                    onDismissRequest = { expandedTheme = false }
                ) {
                    ThemeMode.entries.forEach { themeOption ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (themeOption) {
                                        ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                                        ThemeMode.DARK -> stringResource(R.string.theme_dark)
                                        ThemeMode.BLACK -> stringResource(R.string.theme_black)
                                    }
                                )
                            },
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    settingsViewModel.setThemeMode(themeOption)
                                }
                                expandedTheme = false
                            }
                        )
                    }
                }
            }
            HorizontalDivider()
            Text(
                text = stringResource(R.string.accent_color),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val currentAccentColor = settings.value.accentColor
            val currentCustomHex = settings.value.customAccentColor

            var showColorPickerDialog by remember { mutableStateOf(false) }

            val selectedColor = remember(currentAccentColor, currentCustomHex) {
                if (currentAccentColor == AccentColor.CUSTOM && currentCustomHex != null) {
                    try {
                        Color(AndroidColor.parseColor(currentCustomHex))
                    } catch (e: IllegalArgumentException) {
                        Color.Unspecified // Fallback for invalid hex
                    }
                } else {
                    currentAccentColor.colorInt?.let { Color(it) } ?: Color.Unspecified
                }
            }
            var hexInput by remember(selectedColor) { mutableStateOf(selectedColor.toArgb().toHexString()) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(selectedColor, CircleShape)
                        .clickable { showColorPickerDialog = true }
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { newValue ->
                        hexInput = newValue
                        if (newValue.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$"))) {
                            coroutineScope.launch(Dispatchers.IO) {
                                settingsViewModel.setCustomAccentColor(newValue)
                            }
                        }
                    },
                    label = { Text("Hex Code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (showColorPickerDialog) {
                AlertDialog(
                    onDismissRequest = { showColorPickerDialog = false },
                    title = { Text("Choose Accent Color") },
                    text = {
                        var red by remember { mutableStateOf(selectedColor.red) }
                        var green by remember { mutableStateOf(selectedColor.green) }
                        var blue by remember { mutableStateOf(selectedColor.blue) }

                        Column {
                            OutlinedTextField(
                                value = hexInput,
                                onValueChange = { newValue ->
                                    hexInput = newValue
                                    try {
                                        val color = Color(AndroidColor.parseColor(newValue))
                                        red = color.red
                                        green = color.green
                                        blue = color.blue
                                    } catch (e: IllegalArgumentException) {
                                        // Invalid hex, do nothing or show error
                                    }
                                },
                                label = { Text("Hex Code (e.g., #RRGGBB or #AARRGGBB)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("Red: ${(red * 255).toInt()}")
                            Slider(
                                value = red,
                                onValueChange = {
                                    red = it
                                    hexInput = ComposeColor(red, green, blue).toArgb().toHexString()
                                },
                                valueRange = 0f..1f
                            )
                            Text("Green: ${(green * 255).toInt()}")
                            Slider(
                                value = green,
                                onValueChange = {
                                    green = it
                                    hexInput = ComposeColor(red, green, blue).toArgb().toHexString()
                                },
                                valueRange = 0f..1f
                            )
                            Text("Blue: ${(blue * 255).toInt()}")
                            Slider(
                                value = blue,
                                onValueChange = {
                                    blue = it
                                    hexInput = ComposeColor(red, green, blue).toArgb().toHexString()
                                },
                                valueRange = 0f..1f
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Preset Colors
                            val presetColors = listOf(
                                AccentColor.PURPLE,
                                AccentColor.BLUE,
                                AccentColor.GREEN,
                                AccentColor.ORANGE,
                                AccentColor.RED
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                presetColors.forEach { accentColor ->
                                    accentColor.colorInt?.let { colorInt ->
                                        val color = ComposeColor(colorInt)
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(color, CircleShape)
                                                .clickable {
                                                    coroutineScope.launch(Dispatchers.IO) {
                                                        settingsViewModel.setAccentColor(accentColor)
                                                settingsViewModel.setCustomAccentColor(null) // Clear custom if a preset is chosen
                                            }
                                            hexInput = color.toArgb().toHexString()
                                            red = color.red
                                            green = color.green
                                            blue = color.blue
                                            showColorPickerDialog = false
                                        }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(ComposeColor(red, green, blue), CircleShape)
                                    .align(Alignment.CenterHorizontally)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    settingsViewModel.setCustomAccentColor(hexInput)
                                    settingsViewModel.setAccentColor(AccentColor.CUSTOM) // Set to custom when OK is pressed
                                }
                                showColorPickerDialog = false
                            }
                        ) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showColorPickerDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        SettingsSection(
            heading = stringResource(R.string.settings_section_layout),
        ) {
            Text(
                text = stringResource(R.string.barcode_position),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingsSwitch(
                name = R.string.barcode_top_center,
                switchState = settings.value.barcodePosition == BarcodePosition.Center,
                onCheckedChange = { coroutineScope.launch(Dispatchers.IO) { settingsViewModel.setBarcodePosition(it) } }
            )
            HorizontalDivider()
            SettingsSwitch(
                name = R.string.confirm_delete_dialog,
                switchState = settings.value.confirmDeleteDialog,
                onCheckedChange = { coroutineScope.launch(Dispatchers.IO) { settingsViewModel.setConfirmDeleteDialog(it) } }
            )
            HorizontalDivider()
            Text(
                text = stringResource(R.string.membership_card_image_display_setting_title),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            var expanded by remember { mutableStateOf(false) }
            val selectedOption = settings.value.membershipCardImageDisplay

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = when (selectedOption) {
                        MembershipCardImageDisplay.BACKGROUND -> stringResource(R.string.membership_card_image_display_background)
                        MembershipCardImageDisplay.SMALL -> stringResource(R.string.membership_card_image_display_small)
                        MembershipCardImageDisplay.OFF -> stringResource(R.string.membership_card_image_display_off)
                    },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    MembershipCardImageDisplay.entries.forEach { displayOption ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (displayOption) {
                                        MembershipCardImageDisplay.BACKGROUND -> stringResource(R.string.membership_card_image_display_background)
                                        MembershipCardImageDisplay.SMALL -> stringResource(R.string.membership_card_image_display_small)
                                        MembershipCardImageDisplay.OFF -> stringResource(R.string.membership_card_image_display_off)
                                    }
                                )
                            },
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    settingsViewModel.setMembershipCardImageDisplay(displayOption)
                                }
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        SettingsSection(
            heading = stringResource(R.string.settings_section_refresh),
        ) {
            Text(
                text = stringResource(R.string.enable_sync),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SettingsSwitch(
                name = R.string.enable,
                switchState = settings.value.enableSync,
                onCheckedChange = { coroutineScope.launch(Dispatchers.IO) { settingsViewModel.enableSync(it) } }
            )
            HorizontalDivider()
            Text(
                text = stringResource(R.string.sync_interval),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            SubmittableTextField(
                label = { Text(stringResource(R.string.sync_interval)) },
                initialValue = settings.value.syncInterval.inWholeMinutes.toString(),
                imageVector = Icons.Default.Save,
                inputValidator = { isNaturalNumber(it) },
                onSubmit = {
                    coroutineScope.launch(Dispatchers.IO) { settingsViewModel.setSyncInterval(Integer.parseInt(it).toDuration(
                        DurationUnit.MINUTES)) }
                },
                enabled = settings.value.enableSync,
                clearOnSubmit = false,
            )
        }
    }
}

@Composable
fun SettingsSection(
    heading: String,
    content: @Composable () -> Unit,
) {
    Text(
        text = heading
    )
    ElevatedCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

private fun isNaturalNumber(value: String): Boolean {
    return try {
        val representation = Integer.parseInt(value)
        representation > 0
    } catch (_: NumberFormatException) {
        false
    }
}

fun Int.toHexString(): String {
    return String.format("#%08X", this)
}
