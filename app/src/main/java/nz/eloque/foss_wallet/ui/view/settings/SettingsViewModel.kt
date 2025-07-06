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
        update()
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                enableSync = settingsStore.isSyncEnabled(),
                syncInterval = settingsStore.syncInterval(),
                barcodePosition = settingsStore.barcodePosition(),
                membershipCardImageDisplay = settingsStore.membershipCardImageDisplay(),
                themeMode = settingsStore.themeMode(),
                accentColor = settingsStore.accentColor(),
                customAccentColor = settingsStore.customAccentColor()
            )
        }
    }

    fun enableSync(enabled: Boolean) {
        settingsStore.enableSync(enabled)
        viewModelScope.launch {
            if (enabled) {
                updateScheduler.enableSync()
            } else {
                updateScheduler.disableSync()
            }
            update()
        }
    }
    fun setSyncInterval(duration: Duration) {
        settingsStore.setSyncInterval(duration)
        viewModelScope.launch {
            updateScheduler.updateSyncInterval()
            update()
        }
    }

    fun setBarcodePosition(center: Boolean) {
        settingsStore.setBarcodePosition(if (center) BarcodePosition.Center else BarcodePosition.Top)
        update()
    }

    fun setMembershipCardImageDisplay(display: MembershipCardImageDisplay) {
        settingsStore.setMembershipCardImageDisplay(display)
        update()
    }

    fun setThemeMode(themeMode: ThemeMode) {
        settingsStore.setThemeMode(themeMode)
        update()
    }

    fun setAccentColor(accentColor: AccentColor) {
        settingsStore.setAccentColor(accentColor)
        update()
    }

    fun setCustomAccentColor(hexColor: String?) {
        settingsStore.setCustomAccentColor(hexColor)
        update()
    }

    fun deletePass(pass: Pass) {
        viewModelScope.launch {
            passStore.delete(pass)
        }
    }
}
