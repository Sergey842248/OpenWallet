package nz.eloque.foss_wallet.model

import android.net.Uri
import java.util.Objects

data class MembershipCard(
    val id: String,
    val name: String,
    val code: String,
    val barcodeFormat: BarcodeFormat,
    val frontImageUri: Uri?,
    val backImageUri: Uri?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MembershipCard

        return id == other.id &&
                name == other.name &&
                code == other.code &&
                barcodeFormat == other.barcodeFormat &&
                frontImageUri == other.frontImageUri &&
                backImageUri == other.backImageUri
    }

    override fun hashCode(): Int {
        return Objects.hash(id, name, code, barcodeFormat, frontImageUri, backImageUri)
    }
}
