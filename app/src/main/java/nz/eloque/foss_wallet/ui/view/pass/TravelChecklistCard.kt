package nz.eloque.foss_wallet.ui.view.pass

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import android.app.Activity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import dagger.hilt.android.EntryPointAccessors
import nz.eloque.foss_wallet.model.ChecklistItem
import nz.eloque.foss_wallet.persistence.SettingsStore
import nz.eloque.foss_wallet.ui.view.checklist.ChecklistViewModel
import nz.eloque.foss_wallet.ui.view.checklist.ChecklistViewModelEntryPoint
import nz.eloque.foss_wallet.ui.view.checklist.ChecklistViewModelFactory

@Composable
fun TravelChecklistCard(
    passId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val entryPoint = EntryPointAccessors.fromActivity(context as Activity, ChecklistViewModelEntryPoint::class.java)
    val repository = entryPoint.getChecklistRepository()
    val settingsStore = SettingsStore(androidx.preference.PreferenceManager.getDefaultSharedPreferences(context))
    val viewModel: ChecklistViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = ChecklistViewModelFactory(repository, passId)
    )

    val checklistItems by viewModel.checklistItems.collectAsState()
    val customItems by settingsStore.customChecklistItemsFlow().collectAsState()

    LaunchedEffect(customItems) {
        val initialItems = customItems.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .map { ChecklistItem(passId = passId, text = it, isChecked = false) }
        viewModel.resetAndAddChecklistItems(initialItems)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Travel-List",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            val (checkedItems, uncheckedItems) = checklistItems.partition { it.isChecked }
            val sortedList = uncheckedItems + checkedItems

            sortedList.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { isChecked ->
                            viewModel.updateChecklistItem(item.copy(isChecked = isChecked))
                        }
                    )
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp),
                        textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                    )
                }
            }
        }
    }
}
