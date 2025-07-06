package nz.eloque.foss_wallet.ui.view.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.api.UpdateScheduler
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.persistence.AccentColor
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.persistence.MembershipCardImageDisplay
import nz.eloque.foss_wallet.persistence.PassStore
import nz.eloque.foss_wallet.persistence.SettingsStore
import nz.eloque.foss_wallet.persistence.ThemeMode
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class SettingsUiState(
    val enableSync: Boolean = false,
    val syncInterval: Duration = 1.toDuration(DurationUnit.HOURS),
    val barcodePosition: BarcodePosition = BarcodePosition.Center,
    val membershipCardImageDisplay: MembershipCardImageDisplay = MembershipCardImageDisplay.SMALL,
    val confirmDeleteDialog: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.LIGHT,
    val accentColor: AccentColor = AccentColor.PURPLE,
    val customAccentColor: String? = null,
    val flightPasses: List<Pass> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsStore: SettingsStore,
    private val updateScheduler: UpdateScheduler,
    private val passStore: PassStore, // Inject PassStore
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsStore.confirmDeleteDialogFlow().collect { confirmDelete ->
                _uiState.value = _uiState.value.copy(confirmDeleteDialog = confirmDelete)
            }
        }
        viewModelScope.launch {
            settingsStore.isSyncEnabledFlow().collect { enableSync ->
                _uiState.value = _uiState.value.copy(enableSync = enableSync)
            }
        }
        viewModelScope.launch {
            settingsStore.syncIntervalFlow().collect { syncInterval ->
                _uiState.value = _uiState.value.copy(syncInterval = syncInterval)
            }
        }
        viewModelScope.launch {
            settingsStore.barcodePositionFlow().collect { barcodePosition ->
                _uiState.value = _uiState.value.copy(barcodePosition = barcodePosition)
            }
        }
        viewModelScope.launch {
            settingsStore.membershipCardImageDisplayFlow().collect { membershipCardImageDisplay ->
                _uiState.value = _uiState.value.copy(membershipCardImageDisplay = membershipCardImageDisplay)
            }
        }
        viewModelScope.launch {
            settingsStore.themeModeFlow().collect { themeMode ->
                _uiState.value = _uiState.value.copy(themeMode = themeMode)
            }
        }
        viewModelScope.launch {
            settingsStore.accentColorFlow().collect { accentColor ->
                _uiState.value = _uiState.value.copy(accentColor = accentColor)
            }
        }
        viewModelScope.launch {
            settingsStore.customAccentColorFlow().collect { customAccentColor ->
                _uiState.value = _uiState.value.copy(customAccentColor = customAccentColor)
            }
        }
        // Collect all passes and filter for FlightPasses
        viewModelScope.launch {
            passStore.allPasses().map { passesWithLocalization ->
                passesWithLocalization.filter { it.pass.type is PassType.Boarding }.map { it.pass }
            }.collect { flightPasses ->
                _uiState.value = _uiState.value.copy(flightPasses = flightPasses)
            }
        }
    }

    private fun update() {
        // This function is no longer needed as all settings are collected via flows
        // Keeping it for now in case it's called from other places that need a full refresh
        // However, individual setters now directly update the SettingsStore, which then
        // triggers the flow collection.
    }

    fun enableSync(enabled: Boolean) {
        settingsStore.enableSync(enabled)
        viewModelScope.launch {
            if (enabled) {
                updateScheduler.enableSync()
            } else {
                updateScheduler.disableSync()
            }
        }
    }
    fun setSyncInterval(duration: Duration) {
        settingsStore.setSyncInterval(duration)
        viewModelScope.launch {
            updateScheduler.updateSyncInterval()
        }
    }

    fun setBarcodePosition(center: Boolean) {
        settingsStore.setBarcodePosition(if (center) BarcodePosition.Center else BarcodePosition.Top)
    }

    fun setMembershipCardImageDisplay(display: MembershipCardImageDisplay) {
        settingsStore.setMembershipCardImageDisplay(display)
    }

    fun setConfirmDeleteDialog(enabled: Boolean) {
        settingsStore.setConfirmDeleteDialog(enabled)
    }

    fun setThemeMode(themeMode: ThemeMode) {
        settingsStore.setThemeMode(themeMode)
    }

    fun setAccentColor(accentColor: AccentColor) {
        settingsStore.setAccentColor(accentColor)
    }

    fun setCustomAccentColor(hexColor: String?) {
        settingsStore.setCustomAccentColor(hexColor)
    }

    fun deletePass(pass: Pass) {
        viewModelScope.launch {
            passStore.delete(pass)
        }
    }
}
