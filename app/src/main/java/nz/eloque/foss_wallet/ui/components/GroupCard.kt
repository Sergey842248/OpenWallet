package nz.eloque.foss_wallet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.eloque.foss_wallet.R
import nz.eloque.foss_wallet.model.Pass
import nz.eloque.foss_wallet.persistence.MembershipCardImageDisplay
import nz.eloque.foss_wallet.ui.card.ShortPassCard
import nz.eloque.foss_wallet.ui.view.wallet.PassViewModel
import nz.eloque.foss_wallet.utils.darken

@Composable
fun GroupCard(
    groupId: Long,
    passes: List<Pass>,
    selectedPasses: MutableSet<Pass>,
    passViewModel: PassViewModel,
    modifier: Modifier = Modifier,
    onClick: ((Pass) -> Unit)? = null,
    membershipCardImageDisplay: MembershipCardImageDisplay,
) {
    val coroutineScope = rememberCoroutineScope()

    ElevatedCard(
        modifier = modifier,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(10.dp)
        ) {
            val pagerState = rememberPagerState(0) { passes.size }
            HorizontalPager(
                state = pagerState,
                pageSpacing = 28.dp,
            ) { index ->
                val item = passes[index]
                ShortPassCard(
                    pass = item,
                    colors = CardDefaults.elevatedCardColors().copy(
                        containerColor = CardDefaults.elevatedCardColors().containerColor.darken(
                            1.25f
                        )
                    ),
                    onClick = { onClick?.invoke(item) },
                    membershipCardImageDisplay = membershipCardImageDisplay
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                SelectionIndicator(pagerState.currentPage, passes.size, Modifier.align(Alignment.Center))
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    if (selectedPasses.isNotEmpty()) {
                        IconButton(onClick = { coroutineScope.launch(Dispatchers.IO) { groupId.let {
                            passViewModel.associate(groupId, selectedPasses)
                            selectedPasses.clear()
                        } } }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.ungroup))
                        }
                    }
                    IconButton(onClick = {
                        val selectedPass = passes[pagerState.currentPage]
                        coroutineScope.launch(Dispatchers.IO) { groupId.let { passViewModel.dessociate(selectedPass, groupId) } }
                    }) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = stringResource(R.string.ungroup))
                    }
                    IconButton(onClick = { coroutineScope.launch(Dispatchers.IO) { groupId.let { passViewModel.deleteGroup(it) } } }
                    ) {
                        Icon(imageVector = Icons.Default.FolderDelete, contentDescription = stringResource(R.string.ungroup))
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionIndicator(
    selectedItem: Int,
    itemCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        repeat(itemCount) { index ->
            val isSelected = index == selectedItem
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .size(if (isSelected) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f))
            )
        }
    }
}
