package nz.eloque.foss_wallet.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist")
data class ChecklistItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val passId: String,
    val text: String,
    var isChecked: Boolean
)
