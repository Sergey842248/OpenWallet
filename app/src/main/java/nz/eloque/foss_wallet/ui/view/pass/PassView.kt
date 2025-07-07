package nz.eloque.foss_wallet.ui.view.pass

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.Image
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.field.PassContent
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.persistence.BarcodePosition
import nz.eloque.foss_wallet.ui.card.PassCard
import java.time.Instant
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import nz.eloque.foss_wallet.ui.view.image.ZoomableImage
import nz.eloque.foss_wallet.ui.view.pass.TravelChecklistCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassView(
    pass: Pass,
    barcodePosition: BarcodePosition,
    showTravelChecklist: Boolean,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .verticalScroll(rememberScrollState())
    ) {
        PassCard(pass) { cardColors ->
            Column(
                verticalArrangement = Arrangement.spacedBy(25.dp)
            ) {
                AsyncPassImage(model = pass.footerFile(context), modifier = Modifier.fillMaxWidth())
                BarcodesView(pass.barCodes, barcodePosition)
                if (pass.type is PassType.MembershipCard) {
                    var showFrontImageDialog by remember { mutableStateOf(false) }
                    var showBackImageDialog by remember { mutableStateOf(false) }

                    pass.membershipCard?.frontImageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = stringResource(R.string.image),
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp) // Adjust height as needed
                                .padding(8.dp)
                                .clickable { showFrontImageDialog = true }
                        )
                        if (showFrontImageDialog) {
                            ZoomableImage(imageUri = uri, onDismiss = { showFrontImageDialog = false })
                        }
                    }
                    pass.membershipCard?.backImageUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = stringResource(R.string.image),
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp) // Adjust height as needed
                                .padding(8.dp)
                                .clickable { showBackImageDialog = true }
                        )
                        if (showBackImageDialog) {
                            ZoomableImage(imageUri = uri, onDismiss = { showBackImageDialog = false })
                        }
                    }
                }
            }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(25.dp),
            modifier = Modifier
                .padding(10.dp)
        ) {
            BackFields(pass.backFields)
            val airlineKeywords = listOf("Condor", "Lufthansa", "Eurowings", "Ryanair", "EasyJet", "British Airways", "Air France", "KLM", "American Airlines", "Delta", "United Airlines", "Turkish Airlines", "Qatar Airways")
            val containsAirline = airlineKeywords.any { keyword ->
                pass.organization.contains(keyword, ignoreCase = true) ||
                pass.description.contains(keyword, ignoreCase = true) ||
                pass.backFields.any { it.content.toString().contains(keyword, ignoreCase = true) } ||
                pass.primaryFields.any { it.content.toString().contains(keyword, ignoreCase = true) } ||
                pass.auxiliaryFields.any { it.content.toString().contains(keyword, ignoreCase = true) } ||
                pass.secondaryFields.any { it.content.toString().contains(keyword, ignoreCase = true) }
            }

            if (showTravelChecklist && containsAirline && pass.type !is PassType.MembershipCard) {
                TravelChecklistCard(passId = pass.id)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun PassPreview() {
    val pass = Pass(
        "",
        "KSC - SV Elversberg",
        1,
        "KSC",
        "serial",
        PassType.Generic(),
        HashSet(),
        Instant.ofEpochMilli(0),
        false,
        false,
        false,
        false,
        relevantDate = 1800000000L,
        headerFields = mutableListOf(
            PassField("block", "Block", PassContent.Plain("S1")),
            PassField("seat", "Seat", PassContent.Plain("47")),
        ),
        primaryFields = mutableListOf(
            PassField("name", "Name", PassContent.Plain("Max Mustermann")),
            PassField("seat", "Seat", PassContent.Plain("47")),
        ),
        auxiliaryFields = mutableListOf(
            PassField("block", "Block", PassContent.Plain("S1 | Gegengerade")),
            PassField("seat", "Seat", PassContent.Plain("36E")),
        ),
        secondaryFields = mutableListOf(
            PassField("data1", "data1", PassContent.Plain("Longer Value here i guess")),
            PassField("data2", "data2", PassContent.Plain("Shorter Value")),
        ),
    )
    PassView(pass, BarcodePosition.Center, true)
}
