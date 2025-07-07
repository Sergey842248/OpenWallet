package nz.eloque.foss_wallet.ui.view.settings

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import nz.eloque.foss_wallet.R
import kotlin.math.roundToInt

@Composable
fun ChecklistItemsEditor(
    items: String,
    onItemsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemList = remember(items) {
        items.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
    var newItemText by remember { mutableStateOf("") }

    val onAdd = {
        if (newItemText.isNotBlank()) {
            val newList = itemList + newItemText.trim()
            onItemsChange(newList.joinToString(","))
            newItemText = ""
        }
    }

    var dragIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    var rowHeight by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemList.forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { rowHeight = it.height }
                    .offset { IntOffset(0, if (dragIndex == index) dragOffset.roundToInt() else 0) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier.pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    dragIndex = index
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount.y
                                },
                                onDragEnd = {
                                    val movedItems = (dragOffset / rowHeight).roundToInt()
                                    val newIndex = (index + movedItems).coerceIn(0, itemList.size - 1)
                                    val mutableList = itemList.toMutableList()
                                    val draggedItem = mutableList.removeAt(index)
                                    mutableList.add(newIndex, draggedItem)
                                    onItemsChange(mutableList.joinToString(","))
                                    dragIndex = null
                                    dragOffset = 0f
                                }
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = stringResource(R.string.drag_to_reorder)
                        )
                    }
                    Text(item, modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        val newList = itemList - item
                        onItemsChange(newList.joinToString(","))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = newItemText,
                onValueChange = { newItemText = it },
                label = { Text(stringResource(R.string.add_new_item)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onAdd() }
                ),
                singleLine = true
            )
            Button(onClick = onAdd) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add)
                )
            }
        }
    }
}
