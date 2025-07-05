package nz.eloque.foss_wallet.ui.view.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.api.UpdateScheduler
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.persistence.MembershipCardImageDisplay
import nz.eloque.foss_wallet.persistence.SettingsStore
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

data class SettingsUiState(
    val enableSync: Boolean = false,
    val syncInterval: Duration = 1.toDuration(DurationUnit.HOURS),
    val barcodePosition: BarcodePosition = BarcodePosition.Center,
    val membershipCardImageDisplay: MembershipCardImageDisplay = MembershipCardImageDisplay.SMALL,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsStore: SettingsStore,
    private val updateScheduler: UpdateScheduler,
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        update()
    }

    private fun update() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                enableSync = settingsStore.isSyncEnabled(),
                syncInterval = settingsStore.syncInterval(),
                barcodePosition = settingsStore.barcodePosition(),
                membershipCardImageDisplay = settingsStore.membershipCardImageDisplay()
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
}
