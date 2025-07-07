package nz.eloque.foss_wallet.ui.view.checklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nz.eloque.foss_wallet.persistence.checklist.ChecklistRepository

class ChecklistViewModelFactory(
    private val repository: ChecklistRepository,
    private val passId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChecklistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChecklistViewModel(repository, passId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
