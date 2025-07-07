package nz.eloque.foss_wallet.ui.view.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.model.ChecklistItem
import nz.eloque.foss_wallet.persistence.checklist.ChecklistRepository

class ChecklistViewModel(private val repository: ChecklistRepository, private val passId: String) : ViewModel() {

    val checklistItems: StateFlow<List<ChecklistItem>> = repository.getChecklistItems(passId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateChecklistItem(item: ChecklistItem) {
        viewModelScope.launch {
            repository.update(item)
        }
    }

    fun resetAndAddChecklistItems(items: List<ChecklistItem>) {
        viewModelScope.launch {
            repository.deleteAllForPass(passId)
            repository.insertAll(items)
        }
    }

    fun addChecklistItems(items: List<ChecklistItem>) {
        viewModelScope.launch {
            repository.insertAll(items)
        }
    }
}
