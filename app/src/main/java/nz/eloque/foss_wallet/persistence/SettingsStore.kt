package nz.eloque.foss_wallet.persistence

import android.content.SharedPreferences
import androidx.compose.foundation.layout.Arrangement
import androidx.core.content.edit
import jakarta.inject.Inject
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val SYNC_INTERVAL = "syncInterval"
private const val SYNC_ENABLED = "syncEnabled"
private const val BARCODE_POSITION = "barcodePosition"
private const val MEMBERSHIP_CARD_IMAGE_DISPLAY = "membershipCardImageDisplay"
private const val THEME_MODE = "themeMode"
private const val ACCENT_COLOR_KEY = "accentColor"

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

class SettingsStore @Inject constructor(
    private val prefs: SharedPreferences,
) {
    fun isSyncEnabled(): Boolean = prefs.getBoolean(SYNC_ENABLED, false)

    fun enableSync(enabled: Boolean) = prefs.edit { putBoolean(SYNC_ENABLED, enabled) }

    fun syncInterval(): Duration {
        val amount = prefs.getLong(SYNC_INTERVAL, 60)
        return amount.toDuration(DurationUnit.MINUTES)
    }

    fun setSyncInterval(duration: Duration) = prefs.edit {
        putLong(SYNC_INTERVAL, duration.toLong(DurationUnit.MINUTES))
    }

    fun barcodePosition(): BarcodePosition = BarcodePosition.of(prefs.getString(BARCODE_POSITION, BarcodePosition.Center.key)!!)

    fun setBarcodePosition(barcodePosition: BarcodePosition) = prefs.edit { putString(BARCODE_POSITION, barcodePosition.key) }

    fun membershipCardImageDisplay(): MembershipCardImageDisplay = MembershipCardImageDisplay.of(prefs.getString(MEMBERSHIP_CARD_IMAGE_DISPLAY, MembershipCardImageDisplay.SMALL.key)!!)

    fun setMembershipCardImageDisplay(membershipCardImageDisplay: MembershipCardImageDisplay) = prefs.edit { putString(MEMBERSHIP_CARD_IMAGE_DISPLAY, membershipCardImageDisplay.key) }

    fun themeMode(): ThemeMode = ThemeMode.of(prefs.getString(THEME_MODE, ThemeMode.LIGHT.key)!!)

    fun setThemeMode(themeMode: ThemeMode) = prefs.edit { putString(THEME_MODE, themeMode.key) }

    fun accentColor(): AccentColor = AccentColor.of(prefs.getString(ACCENT_COLOR_KEY, AccentColor.PURPLE.key)!!)

    fun setAccentColor(accentColor: AccentColor) = prefs.edit { putString(ACCENT_COLOR_KEY, accentColor.key) }
}

enum class AccentColor(val key: String, val colorInt: Int) {
    PURPLE("PURPLE", 0xFF6200EE.toInt()),
    BLUE("BLUE", 0xFF2196F3.toInt()),
    GREEN("GREEN", 0xFF4CAF50.toInt()),
    ORANGE("ORANGE", 0xFFFF9800.toInt()),
    RED("RED", 0xFFF44336.toInt());

    companion object {
        fun of(representation: String): AccentColor {
            return when (representation) {
                PURPLE.key -> PURPLE
                BLUE.key -> BLUE
                GREEN.key -> GREEN
                ORANGE.key -> ORANGE
                RED.key -> RED
                else -> PURPLE
            }
        }
    }
}
