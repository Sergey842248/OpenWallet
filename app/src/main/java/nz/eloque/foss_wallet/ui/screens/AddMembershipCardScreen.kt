package nz.eloque.foss_wallet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMembershipCardScreen(
    navController: NavHostController,
    passViewModel: PassViewModel = hiltViewModel()
) {
    var code by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var selectedFormat by remember { mutableStateOf(BarcodeFormat.QR_CODE) }
    var name by remember { mutableStateOf("") }
    var frontImageUri by remember { mutableStateOf<Uri?>(null) }
    var backImageUri by remember { mutableStateOf<Uri?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val frontImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        frontImageUri = uri
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    val backImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        backImageUri = uri
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    WalletScaffold(
        navController = navController,
        title = stringResource(id = R.string.add_membership_card)
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
                modifier = Modifier.fillMaxWidth(),
                isError = name.isBlank()
            )

            TextField(
                value = code,
                onValueChange = { code = it },
                label = { Text(stringResource(id = R.string.manual_code_entry)) },
                modifier = Modifier.fillMaxWidth(),
                isError = code.isBlank()
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

            Button(
                onClick = {
                    coroutineScope.launch {
                        val frontBitmap = withContext(Dispatchers.IO) {
                            try {
                                frontImageUri?.let { uri ->
                                    if (Build.VERSION.SDK_INT < 28) {
                                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                    } else {
                                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                                        ImageDecoder.decodeBitmap(source)
                                    }
                                }
                            } catch (e: Exception) {
                                null
                            }
                        } ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

                        val backBitmap = withContext(Dispatchers.IO) {
                            try {
                                backImageUri?.let { uri ->
                                    if (Build.VERSION.SDK_INT < 28) {
                                        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                                    } else {
                                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                                        ImageDecoder.decodeBitmap(source)
                                    }
                                }
                            } catch (e: Exception) {
                                null
                            }
                        } ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)

                        val newPass = Pass(
                            id = UUID.randomUUID().toString(),
                            description = "Membership Card",
                            formatVersion = 1,
                            organization = "",
                            serialNumber = "",
                            type = PassType.MembershipCard(),
                            barCodes = setOf(BarCode(ZxingBarcodeFormat.valueOf(selectedFormat.name), code, Charsets.UTF_8, null)),
                            addedAt = Instant.now(),
                            hasThumbnail = false, // Images should not be displayed above QR/Barcode on new creation
                            hasStrip = false, // Images should not be displayed above QR/Barcode on new creation
                            membershipCard = MembershipCard(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                code = code,
                                barcodeFormat = selectedFormat,
                                frontImageUri = frontImageUri,
                                backImageUri = backImageUri
                            )
                        )
                        val passWithLocalization = PassWithLocalization(newPass, emptyList())
                        passViewModel.add(PassLoadResult(passWithLocalization, PassBitmaps(
                            logo = null,
                            strip = null, // Do not save strip bitmap for new membership cards
                            thumbnail = null, // Do not save thumbnail bitmap for new membership cards
                            footer = null,
                            icon = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Ensure icon is not null and not the front image
                        ), OriginalPass(byteArrayOf())))
                        navController.popBackStack()
                    }
                },
                enabled = name.isNotBlank() && code.isNotEmpty()
            ) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    }
}
