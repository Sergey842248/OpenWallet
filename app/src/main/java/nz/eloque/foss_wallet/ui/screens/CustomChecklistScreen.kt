package nz.eloque.foss_wallet.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.ui.view.settings.ChecklistItemsEditor
import nz.eloque.foss_wallet.ui.view.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomChecklistScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
) {
    val settings = settingsViewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.custom_travellist_items)) },
                navigationIcon = {
                    BackButton(navController)
                }
            )
        }
    ) { innerPadding ->
        ChecklistItemsEditor(
            items = settings.value.customChecklistItems,
            onItemsChange = { newItems ->
                coroutineScope.launch(Dispatchers.IO) {
                    settingsViewModel.setCustomChecklistItems(newItems)
                }
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
fun BackButton(navController: NavController) {
    IconButton(onClick = { navController.popBackStack() }) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.back)
        )
    }
}
