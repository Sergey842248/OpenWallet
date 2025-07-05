package nz.eloque.foss_wallet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import nz.eloque.foss_wallet.R

@Composable
fun AddPassDialog(
    onDismissRequest: () -> Unit,
    onFlightPassClick: () -> Unit,
    onMembershipCardClick: () -> Unit
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = stringResource(id = R.string.add_pass_title), style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
                Button(onClick = { onFlightPassClick() }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.flight_pass))
                }
                Button(onClick = { onMembershipCardClick() }, modifier = Modifier.fillMaxWidth()) {
                    Text(text = stringResource(id = R.string.membership_card))
                }
            }
        }
    }
}
