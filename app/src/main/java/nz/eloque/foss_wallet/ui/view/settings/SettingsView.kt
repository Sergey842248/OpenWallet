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
            var expandedAccent by remember { mutableStateOf(false) }
            val selectedAccentOption = settings.value.accentColor

            ExposedDropdownMenuBox(
                expanded = expandedAccent,
                onExpandedChange = { expandedAccent = !expandedAccent },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = when (selectedAccentOption) {
                        AccentColor.PURPLE -> stringResource(R.string.accent_color_purple)
                        AccentColor.BLUE -> stringResource(R.string.accent_color_blue)
                        AccentColor.GREEN -> stringResource(R.string.accent_color_green)
                        AccentColor.ORANGE -> stringResource(R.string.accent_color_orange)
                        AccentColor.RED -> stringResource(R.string.accent_color_red)
                    },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccent) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expandedAccent,
                    onDismissRequest = { expandedAccent = false }
                ) {
                    AccentColor.entries.forEach { accentOption ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (accentOption) {
                                        AccentColor.PURPLE -> stringResource(R.string.accent_color_purple)
                                        AccentColor.BLUE -> stringResource(R.string.accent_color_blue)
                                        AccentColor.GREEN -> stringResource(R.string.accent_color_green)
                                        AccentColor.ORANGE -> stringResource(R.string.accent_color_orange)
                                        AccentColor.RED -> stringResource(R.string.accent_color_red)
                                    }
                                )
                            },
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    settingsViewModel.setAccentColor(accentOption)
                                }
                                expandedAccent = false
                            }
                        )
                    }
                }
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
