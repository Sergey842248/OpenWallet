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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import android.app.Activity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.first
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
    val coroutineScope = rememberCoroutineScope()

    var visualCheckedState by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    LaunchedEffect(checklistItems) {
        visualCheckedState = checklistItems.associate { it.text to it.isChecked }
    }

    LaunchedEffect(customItems) {
        val customItemsList = customItems.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val currentItems = repository.getChecklistItems(passId).first()
        val currentItemsMap = currentItems.associateBy { it.text }
        val newItems = customItemsList.map {
            ChecklistItem(
                passId = passId,
                text = it,
                isChecked = currentItemsMap[it]?.isChecked ?: false
            )
        }
        viewModel.resetAndAddChecklistItems(newItems)
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
                    val isChecked = visualCheckedState[item.text] ?: item.isChecked
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { newCheckedState ->
                            visualCheckedState = visualCheckedState + (item.text to newCheckedState)

                            coroutineScope.launch {
                                delay(80)
                                viewModel.updateChecklistItem(item.copy(isChecked = newCheckedState))
                            }
                        }
                    )
                    Text(
                        text = item.text,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp),
                        textDecoration = if (isChecked) TextDecoration.LineThrough else null
                    )
                }
            }
        }
    }
}
