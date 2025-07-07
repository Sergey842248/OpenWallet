package nz.eloque.foss_wallet.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.ChecklistItem

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist WHERE passId = :passId")
    fun getChecklistItems(passId: String): Flow<List<ChecklistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ChecklistItem>)

    @Update
    suspend fun update(item: ChecklistItem)

    @Query("DELETE FROM checklist WHERE passId = :passId")
    suspend fun deleteAllForPass(passId: String)
}
