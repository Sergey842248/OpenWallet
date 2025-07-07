package nz.eloque.foss_wallet.persistence.checklist

import kotlinx.coroutines.flow.Flow
import nz.eloque.foss_wallet.model.ChecklistItem
import nz.eloque.foss_wallet.persistence.ChecklistDao

class ChecklistRepository(private val checklistDao: ChecklistDao) {
    fun getChecklistItems(passId: String): Flow<List<ChecklistItem>> {
        return checklistDao.getChecklistItems(passId)
    }

    suspend fun insertAll(items: List<ChecklistItem>) {
        checklistDao.insertAll(items)
    }

    suspend fun update(item: ChecklistItem) {
        checklistDao.update(item)
    }

    suspend fun deleteAllForPass(passId: String) {
        checklistDao.deleteAllForPass(passId)
    }
}
