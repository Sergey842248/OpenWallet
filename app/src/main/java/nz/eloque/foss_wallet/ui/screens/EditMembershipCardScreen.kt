package nz.eloque.foss_wallet.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import android.graphics.Bitmap
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.BarCode
import nz.eloque.foss_wallet.model.BarcodeFormat
import nz.eloque.foss_wallet.model.MembershipCard
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.PassWithLocalization
import nz.eloque.foss_wallet.model.OriginalPass
import nz.eloque.foss_wallet.persistence.PassBitmaps
import nz.eloque.foss_wallet.persistence.PassLoadResult
import nz.eloque.foss_wallet.ui.WalletScaffold
import nz.eloque.foss_wallet.ui.view.wallet.PassViewModel
import com.google.zxing.BarcodeFormat as ZxingBarcodeFormat
import java.nio.charset.Charset
import java.time.Instant
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMembershipCardScreen(
    passId: String,
    navController: NavHostController,
    passViewModel: PassViewModel = hiltViewModel()
) {
    var code by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf(BarcodeFormat.QR_CODE) }
    var name by remember { mutableStateOf("") }
    var frontImageUri by remember { mutableStateOf<Uri?>(null) }
    var backImageUri by remember { mutableStateOf<Uri?>(null) }
    var currentPass by remember { mutableStateOf<Pass?>(null) }

    val frontImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        frontImageUri = uri
    }

    val backImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        backImageUri = uri
    }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(passId) {
        coroutineScope.launch(Dispatchers.IO) {
            val pass = passViewModel.passById(passId).applyLocalization(Locale.getDefault().language)
            withContext(Dispatchers.Main) {
                currentPass = pass
                pass.membershipCard?.let {
                    name = it.name
                    code = it.code
                    selectedFormat = it.barcodeFormat
                    frontImageUri = it.frontImageUri
                    backImageUri = it.backImageUri
                }
            }
        }
    }

    WalletScaffold(
        navController = navController,
        title = stringResource(id = R.string.edit)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.membership_card_name)) },
                modifier = Modifier.fillMaxWidth()
            )

            TextField(
                value = code,
                onValueChange = { code = it },
                label = { Text(stringResource(id = R.string.manual_code_entry)) },
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = selectedFormat.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.barcode_type)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    BarcodeFormat.values().forEach { format ->
                        DropdownMenuItem(
                            text = { Text(format.name) },
                            onClick = {
                                selectedFormat = format
                                expanded = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { frontImageLauncher.launch("image/*") }) {
                    Text(text = stringResource(id = R.string.add_front_image))
                }
                Button(onClick = { backImageLauncher.launch("image/*") }) {
                    Text(text = stringResource(id = R.string.add_back_image))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                frontImageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Front of card",
                        modifier = Modifier.size(128.dp)
                    )
                }
                backImageUri?.let {
                    Image(
                        painter = rememberAsyncImagePainter(it),
                        contentDescription = "Back of card",
                        modifier = Modifier.size(128.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                currentPass?.let { pass ->
                    val updatedMembershipCard = pass.membershipCard?.copy(
                        name = name,
                        code = code,
                        barcodeFormat = selectedFormat,
                        frontImageUri = frontImageUri,
                        backImageUri = backImageUri
                    )
                    val updatedPass = pass.copy(
                        description = name, // Update description to reflect the new name
                        barCodes = setOf(BarCode(ZxingBarcodeFormat.valueOf(selectedFormat.name), code, Charsets.UTF_8, null)),
                        membershipCard = updatedMembershipCard
                    )

                    // Handle bitmaps for update, similar to AddMembershipCardScreen
                    val frontBitmap = try {
                        frontImageUri?.let { uri ->
                            if (android.os.Build.VERSION.SDK_INT < 28) {
                                android.provider.MediaStore.Images.Media.getBitmap(navController.context.contentResolver, uri)
                            } else {
                                val source = android.graphics.ImageDecoder.createSource(navController.context.contentResolver, uri)
                                android.graphics.ImageDecoder.decodeBitmap(source)
                            }
                        }
                    } catch (e: Exception) {
                        null
                    } ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

                    val backBitmap = try {
                        backImageUri?.let { uri ->
                            if (android.os.Build.VERSION.SDK_INT < 28) {
                                android.provider.MediaStore.Images.Media.getBitmap(navController.context.contentResolver, uri)
                            } else {
                                val source = android.graphics.ImageDecoder.createSource(navController.context.contentResolver, uri)
                                android.graphics.ImageDecoder.decodeBitmap(source)
                            }
                        }
                    } catch (e: Exception) {
                        null
                    } ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

                    coroutineScope.launch {
                        // PassLoadResult is typically for adding new passes, for updates we might need a different approach
                        // For now, we'll just update the pass object directly.
                        // If image saving is tied to PassLoadResult, we might need to refactor PassStore.update
                        passViewModel.update(updatedPass)
                        navController.popBackStack()
                    }
                }
            }) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    }
}
