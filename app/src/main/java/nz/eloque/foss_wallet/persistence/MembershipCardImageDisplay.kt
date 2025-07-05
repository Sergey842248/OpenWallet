package nz.eloque.foss_wallet.persistence

enum class MembershipCardImageDisplay(val key: String) {
    BACKGROUND("BACKGROUND"),
    SMALL("SMALL"),
    OFF("OFF");

    companion object {
        fun of(representation: String): MembershipCardImageDisplay {
            return when (representation) {
                BACKGROUND.key -> BACKGROUND
                SMALL.key -> SMALL
                OFF.key -> OFF
                else -> SMALL // Default to SMALL if not found
            }
        }
    }
}
