package nz.eloque.foss_wallet.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.persistence.InvalidPassException
import nz.eloque.foss_wallet.ui.Screen
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.ui.view.settings.SettingsViewModel
import nz.eloque.foss_wallet.ui.view.wallet.PassViewModel
import nz.eloque.foss_wallet.ui.view.wallet.WalletView
import nz.eloque.foss_wallet.utils.isScrollingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    navController: NavHostController,
    passViewModel: PassViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val coroutineScope = rememberCoroutineScope()

    val listState = rememberLazyListState()
    val toastMessage = stringResource(R.string.invalid_pass_toast)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { res ->
        res?.let {
            println("selected file URI $res")
            coroutineScope.launch(Dispatchers.IO) {
                contentResolver.openInputStream(res)?.use { inputStream ->
                    try {
                        passViewModel.load(context, inputStream)
                    } catch (_: InvalidPassException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
    val selectedPasses = remember { mutableStateSetOf<Pass>() }
    var showAddOptions by remember { mutableStateOf(false) }
    val settings by settingsViewModel.uiState.collectAsState()
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    WalletScaffold(
        navController = navController,
        title = stringResource(id = R.string.wallet),
        actions = {
            IconButton(onClick = {
                navController.navigate(Screen.Settings.route)
            }) {
                Icon(
                    imageVector = Screen.Settings.icon,
                    contentDescription = stringResource(R.string.about)
                )
            }
        },
        floatingActionButton = {
            if (selectedPasses.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.error,
                        onClick = {
                            if (settings.confirmDeleteDialog) {
                                showDeleteConfirmationDialog = true
                            } else {
                                coroutineScope.launch(Dispatchers.IO) {
                                    selectedPasses.forEach { passViewModel.delete(it) }
                                    selectedPasses.clear()
                                }
                            }
                        },
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete))
                    }
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(R.string.group)) },
                        icon = { Icon(imageVector = Icons.Default.Folder, contentDescription = stringResource(R.string.group)) },
                        expanded = listState.isScrollingUp(),
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                passViewModel.group(selectedPasses.toSet())
                                selectedPasses.clear()
                            }
                        },
                    )
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    AnimatedVisibility(
                        visible = showAddOptions,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            ExtendedFloatingActionButton(
                                text = { Text(stringResource(R.string.pass)) },
                                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_flight_pass)) },
                                onClick = {
                                    showAddOptions = false
                                    launcher.launch(arrayOf("*/*"))
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                            ExtendedFloatingActionButton(
                                text = { Text(stringResource(R.string.membership)) },
                                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add_membership_card)) },
                                onClick = {
                                    showAddOptions = false
                                    navController.navigate(Screen.AddMembershipCard.route)
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        }
                    }
                    ExtendedFloatingActionButton(
                        text = { Text(if (showAddOptions) stringResource(R.string.cancel) else stringResource(R.string.add)) },
                        icon = { Icon(imageVector = if (showAddOptions) Icons.Default.Close else Icons.Default.Add, contentDescription = if (showAddOptions) stringResource(R.string.cancel) else stringResource(R.string.add_pass)) },
                        expanded = listState.isScrollingUp(), // Revert to original expanded state
                        onClick = { showAddOptions = !showAddOptions },
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
    ) { scrollBehavior ->
        Box(modifier = Modifier.fillMaxSize()) {
            WalletView(
                navController,
                passViewModel,
                listState = listState,
                scrollBehavior = scrollBehavior,
                selectedPasses = selectedPasses,
                membershipCardImageDisplay = settings.membershipCardImageDisplay
            )

            AnimatedVisibility(
                visible = showAddOptions,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { showAddOptions = false }
                )
            }
        }

        if (showDeleteConfirmationDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmationDialog = false },
                title = { Text(stringResource(R.string.delete)) },
                text = { Text(stringResource(R.string.confirm_delete_dialog_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            coroutineScope.launch(Dispatchers.IO) {
                                selectedPasses.forEach { passViewModel.delete(it) }
                                selectedPasses.clear()
                            }
                            showDeleteConfirmationDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmationDialog = false }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}
