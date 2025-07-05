package nz.eloque.foss_wallet.ui.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.model.PassType
import nz.eloque.foss_wallet.model.TransitType
import nz.eloque.foss_wallet.model.field.PassField
import nz.eloque.foss_wallet.persistence.MembershipCardImageDisplay
import nz.eloque.foss_wallet.ui.card.primary.AirlineBoardingPrimary
import nz.eloque.foss_wallet.ui.card.primary.GenericBoardingPrimary
import nz.eloque.foss_wallet.ui.card.primary.GenericPrimary
import nz.eloque.foss_wallet.ui.view.pass.AsyncPassImage
import androidx.compose.ui.draw.paint
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Color


@Composable
fun ShortPassContent(
    pass: Pass,
    cardColors: CardColors,
    modifier: Modifier = Modifier,
    membershipCardImageDisplay: MembershipCardImageDisplay,
) {
    val context = LocalContext.current
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        if (pass.type is PassType.MembershipCard) {
            val imageUri = pass.membershipCard?.frontImageUri ?: pass.membershipCard?.backImageUri
            val painter: Painter? = imageUri?.let { rememberAsyncImagePainter(it) }

            when (membershipCardImageDisplay) {
                MembershipCardImageDisplay.BACKGROUND -> {
                    if (painter != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp) // Adjust height as needed for background
                                .paint(
                                    painter = painter,
                                    contentScale = ContentScale.Crop,
                                    colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.3f), BlendMode.SrcOver) // Optional: darken image for text readability
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                pass.membershipCard?.name?.let {
                                    Text(
                                        text = it,
                                        maxLines = 1,
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = Color.White, // Ensure text is visible on dark background
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            pass.membershipCard?.name?.let {
                                Text(
                                    text = it,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                MembershipCardImageDisplay.SMALL -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        if (painter != null) {
                            Image(
                                painter = painter,
                                contentDescription = stringResource(R.string.image),
                                contentScale = ContentScale.FillHeight,
                                modifier = Modifier.padding(5.dp)
                                    .height(64.dp)
                            )
                        }
                        pass.membershipCard?.name?.let {
                            Text(
                                text = it,
                                maxLines = 1,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                MembershipCardImageDisplay.OFF -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        pass.membershipCard?.name?.let {
                            Text(
                                text = it,
                                maxLines = 1,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        } else {
            HeaderRow(pass)
            when (pass.type) {
                is PassType.Boarding ->
                    when (pass.type.transitType) {
                        TransitType.AIR -> AirlineBoardingPrimary(pass, cardColors)
                        else -> GenericBoardingPrimary(pass, pass.type.transitType, cardColors)
                    }
                else -> GenericPrimary(pass)
            }
            if (pass.primaryFields.empty() && pass.hasStrip) {
                AsyncPassImage(model = pass.stripFile(context), modifier = Modifier.fillMaxWidth())
            }
        }

        DateLocationRow(pass)
    }
}

private fun List<PassField>.empty(): Boolean {
    return this.isEmpty() || this.all { it.content.isEmpty() }
}

@Composable
fun PassContent(
    pass: Pass,
    cardColors: CardColors,
    modifier: Modifier = Modifier,
    content: @Composable ((CardColors) -> Unit)
) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
    ) {
        HeaderRow(pass)
        when (pass.type) {
            is PassType.Boarding ->
                when (pass.type.transitType) {
                    TransitType.AIR -> AirlineBoardingPrimary(pass, cardColors)
                    else -> GenericBoardingPrimary(pass, pass.type.transitType, cardColors)
                }
            else -> GenericPrimary(pass)
        }
        AsyncPassImage(model = pass.stripFile(context), modifier = Modifier.fillMaxWidth())
        FieldsRow(pass.secondaryFields)
        FieldsRow(pass.auxiliaryFields)

        content.invoke(cardColors)

        DateLocationRow(pass)
    }
}
