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
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.persistence.MembershipCardImageDisplay
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    settingsViewModel: SettingsViewModel,
) {
    val coroutineScope = rememberCoroutineScope()
    val settings = settingsViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SettingsSection(
            heading = stringResource(R.string.enable_sync),
        ) {
            SettingsSwitch(
                name = R.string.enable,
                switchState = settings.value.enableSync,
                onCheckedChange = { coroutineScope.launch(Dispatchers.IO) { settingsViewModel.enableSync(it) } }
            )
            HorizontalDivider()
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
        SettingsSection(
            heading = stringResource(R.string.barcode_position),
        ) {
            SettingsSwitch(
                name = R.string.barcode_top_center,
                switchState = settings.value.barcodePosition == BarcodePosition.Center,
                onCheckedChange = { coroutineScope.launch(Dispatchers.IO) { settingsViewModel.setBarcodePosition(it) } }
            )
        }
        SettingsSection(
            heading = stringResource(R.string.membership_card_image_display_setting_title),
        ) {
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
