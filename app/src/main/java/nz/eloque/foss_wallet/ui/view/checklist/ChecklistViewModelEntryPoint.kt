package nz.eloque.foss_wallet.ui.view.checklist

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import nz.eloque.foss_wallet.persistence.checklist.ChecklistRepository

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ChecklistViewModelEntryPoint {
    fun getChecklistRepository(): ChecklistRepository
}
