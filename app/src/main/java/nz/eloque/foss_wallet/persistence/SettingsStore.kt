package nz.eloque.foss_wallet.persistence

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.core.content.edit
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val SYNC_INTERVAL = "syncInterval"
private const val SYNC_ENABLED = "syncEnabled"
private const val BARCODE_POSITION = "barcodePosition"
private const val MEMBERSHIP_CARD_IMAGE_DISPLAY = "membershipCardImageDisplay"
private const val CONFIRM_DELETE_DIALOG = "confirmDeleteDialog"
private const val THEME_MODE = "themeMode"
private const val ACCENT_COLOR_KEY = "accentColor"
private const val CUSTOM_ACCENT_COLOR_KEY = "customAccentColor"
private const val SHOW_TRAVEL_CHECKLIST = "showTravelChecklist"
private const val CUSTOM_CHECKLIST_ITEMS = "customChecklistItems"

sealed class BarcodePosition(val arrangement: Arrangement.Vertical, val key: String) {
    object Top : BarcodePosition(Arrangement.Top, "TOP")
    object Center : BarcodePosition(Arrangement.Center, "CENTER")

    companion object {
        fun of(representation: String): BarcodePosition {
            return when (representation) {
                Top.key -> Top
                Center.key -> Center
                else -> Center
            }
        }
    }
}

enum class ThemeMode(val key: String) {
    LIGHT("LIGHT"),
    DARK("DARK"),
    BLACK("BLACK");

    companion object {
        fun of(representation: String): ThemeMode {
            return when (representation) {
                LIGHT.key -> LIGHT
                DARK.key -> DARK
                BLACK.key -> BLACK
                else -> LIGHT
            }
        }
    }
}

enum class AccentColor(val key: String, val colorInt: Int?) {
    PURPLE("PURPLE", 0xFF6200EE.toInt()),
    BLUE("BLUE", 0xFF2196F3.toInt()),
    GREEN("GREEN", 0xFF4CAF50.toInt()),
    ORANGE("ORANGE", 0xFFFF9800.toInt()),
    RED("RED", 0xFFF44336.toInt()),
    CUSTOM("CUSTOM", null);

    companion object {
        fun of(representation: String): AccentColor {
            return entries.firstOrNull { it.key.equals(representation, ignoreCase = true) } ?: PURPLE
        }
    }
}

class SettingsStore @Inject constructor(
    private val prefs: SharedPreferences,
) {
    private val _syncEnabledFlow = MutableStateFlow(isSyncEnabled())
    private val _syncIntervalFlow = MutableStateFlow(syncInterval())
    private val _barcodePositionFlow = MutableStateFlow(barcodePosition())
    private val _membershipCardImageDisplayFlow = MutableStateFlow(membershipCardImageDisplay())
    private val _confirmDeleteDialogFlow = MutableStateFlow(confirmDeleteDialog())
    private val _themeModeFlow = MutableStateFlow(themeMode())
    private val _accentColorFlow = MutableStateFlow(accentColor())
    private val _customAccentColorFlow = MutableStateFlow(customAccentColor())
    private val _showTravelChecklistFlow = MutableStateFlow(showTravelChecklist())
    private val _customChecklistItemsFlow = MutableStateFlow(customChecklistItems())

    private val listener = OnSharedPreferenceChangeListener { sharedPreferences, key ->
        when (key) {
            SYNC_ENABLED -> _syncEnabledFlow.value = sharedPreferences.getBoolean(SYNC_ENABLED, false)
            SYNC_INTERVAL -> _syncIntervalFlow.value = sharedPreferences.getLong(SYNC_INTERVAL, 60).toDuration(DurationUnit.MINUTES)
            BARCODE_POSITION -> _barcodePositionFlow.value = BarcodePosition.of(sharedPreferences.getString(BARCODE_POSITION, BarcodePosition.Center.key)!!)
            MEMBERSHIP_CARD_IMAGE_DISPLAY -> _membershipCardImageDisplayFlow.value = MembershipCardImageDisplay.of(sharedPreferences.getString(MEMBERSHIP_CARD_IMAGE_DISPLAY, MembershipCardImageDisplay.SMALL.key)!!)
            CONFIRM_DELETE_DIALOG -> _confirmDeleteDialogFlow.value = sharedPreferences.getBoolean(CONFIRM_DELETE_DIALOG, true)
            THEME_MODE -> _themeModeFlow.value = ThemeMode.of(sharedPreferences.getString(THEME_MODE, ThemeMode.LIGHT.key)!!)
            ACCENT_COLOR_KEY -> _accentColorFlow.value = AccentColor.of(sharedPreferences.getString(ACCENT_COLOR_KEY, AccentColor.PURPLE.key)!!)
            CUSTOM_ACCENT_COLOR_KEY -> _customAccentColorFlow.value = sharedPreferences.getString(CUSTOM_ACCENT_COLOR_KEY, null)
            SHOW_TRAVEL_CHECKLIST -> _showTravelChecklistFlow.value = sharedPreferences.getBoolean(SHOW_TRAVEL_CHECKLIST, true)
            CUSTOM_CHECKLIST_ITEMS -> _customChecklistItemsFlow.value = sharedPreferences.getString(CUSTOM_CHECKLIST_ITEMS, "")!!
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun isSyncEnabled(): Boolean = prefs.getBoolean(SYNC_ENABLED, false)
    fun isSyncEnabledFlow(): StateFlow<Boolean> = _syncEnabledFlow.asStateFlow()

    fun enableSync(enabled: Boolean) = prefs.edit { putBoolean(SYNC_ENABLED, enabled) }

    fun syncInterval(): Duration {
        val amount = prefs.getLong(SYNC_INTERVAL, 60)
        return amount.toDuration(DurationUnit.MINUTES)
    }
    fun syncIntervalFlow(): StateFlow<Duration> = _syncIntervalFlow.asStateFlow()

    fun setSyncInterval(duration: Duration) = prefs.edit {
        putLong(SYNC_INTERVAL, duration.toLong(DurationUnit.MINUTES))
    }

    fun barcodePosition(): BarcodePosition = BarcodePosition.of(prefs.getString(BARCODE_POSITION, BarcodePosition.Center.key)!!)
    fun barcodePositionFlow(): StateFlow<BarcodePosition> = _barcodePositionFlow.asStateFlow()

    fun setBarcodePosition(barcodePosition: BarcodePosition) = prefs.edit { putString(BARCODE_POSITION, barcodePosition.key) }

    fun membershipCardImageDisplay(): MembershipCardImageDisplay = MembershipCardImageDisplay.of(prefs.getString(MEMBERSHIP_CARD_IMAGE_DISPLAY, MembershipCardImageDisplay.SMALL.key)!!)
    fun membershipCardImageDisplayFlow(): StateFlow<MembershipCardImageDisplay> = _membershipCardImageDisplayFlow.asStateFlow()

    fun setMembershipCardImageDisplay(membershipCardImageDisplay: MembershipCardImageDisplay) = prefs.edit { putString(MEMBERSHIP_CARD_IMAGE_DISPLAY, membershipCardImageDisplay.key) }

    fun confirmDeleteDialog(): Boolean = prefs.getBoolean(CONFIRM_DELETE_DIALOG, true)
    fun confirmDeleteDialogFlow(): StateFlow<Boolean> = _confirmDeleteDialogFlow.asStateFlow()

    fun setConfirmDeleteDialog(enabled: Boolean) = prefs.edit { putBoolean(CONFIRM_DELETE_DIALOG, enabled) }

    fun showTravelChecklist(): Boolean = prefs.getBoolean(SHOW_TRAVEL_CHECKLIST, true)
    fun showTravelChecklistFlow(): StateFlow<Boolean> = _showTravelChecklistFlow.asStateFlow()
    fun setShowTravelChecklist(enabled: Boolean) = prefs.edit { putBoolean(SHOW_TRAVEL_CHECKLIST, enabled) }

    fun customChecklistItems(): String = prefs.getString(CUSTOM_CHECKLIST_ITEMS, "")!!
    fun customChecklistItemsFlow(): StateFlow<String> = _customChecklistItemsFlow.asStateFlow()
    fun setCustomChecklistItems(items: String) = prefs.edit { putString(CUSTOM_CHECKLIST_ITEMS, items) }

    fun themeMode(): ThemeMode = ThemeMode.of(prefs.getString(THEME_MODE, ThemeMode.LIGHT.key)!!)
    fun themeModeFlow(): StateFlow<ThemeMode> = _themeModeFlow.asStateFlow()

    fun setThemeMode(themeMode: ThemeMode) = prefs.edit { putString(THEME_MODE, themeMode.key) }

    fun accentColor(): AccentColor = AccentColor.of(prefs.getString(ACCENT_COLOR_KEY, AccentColor.PURPLE.key)!!)
    fun accentColorFlow(): StateFlow<AccentColor> = _accentColorFlow.asStateFlow()

    fun setAccentColor(accentColor: AccentColor) = prefs.edit { putString(ACCENT_COLOR_KEY, accentColor.key) }

    fun customAccentColor(): String? = prefs.getString(CUSTOM_ACCENT_COLOR_KEY, null)
    fun customAccentColorFlow(): StateFlow<String?> = _customAccentColorFlow.asStateFlow()

    fun setCustomAccentColor(hexColor: String?) = prefs.edit { putString(CUSTOM_ACCENT_COLOR_KEY, hexColor) }
}
