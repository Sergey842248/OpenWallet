package nz.eloque.foss_wallet.ui.card

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.persistence.MembershipCardImageDisplay

@Composable
fun ShortPassCard(
    pass: Pass,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    membershipCardImageDisplay: MembershipCardImageDisplay,
) {
    val cardColors = pass.colors?.toCardColors() ?: colors
    val scale by animateFloatAsState(if (selected) 0.95f else 1f)
    if (onClick == null) {
        ElevatedCard(
            colors = cardColors,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
                .padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            ShortPassContent(pass, cardColors, membershipCardImageDisplay = membershipCardImageDisplay)
        }
    } else {
        ElevatedCard(
            onClick = onClick,
            colors = cardColors,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ShortPassContent(pass, cardColors, membershipCardImageDisplay = membershipCardImageDisplay)
        }
    }
}

@Composable
fun PassCard(
    pass: Pass,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    selected: Boolean = false,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    content: @Composable ((cardColors: CardColors) -> Unit),
) {
    val cardColors = pass.colors?.toCardColors() ?: colors
    val scale by animateFloatAsState(if (selected) 0.95f else 1f)
    if (onClick == null) {
        ElevatedCard(
            colors = cardColors,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            PassContent(pass, cardColors, Modifier, content)
        }
    } else {
        ElevatedCard(
            onClick = onClick,
            colors = cardColors,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
            modifier = modifier
                .fillMaxWidth()
                .scale(scale)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            PassContent(pass, cardColors, Modifier, content)
        }
    }
}

@Preview
@Composable
private fun PasscardPreview() {
    PassCard(
        pass = Pass.placeholder(),
    ) {

    }
}
