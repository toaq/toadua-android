package town.robin.toadua.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Workspaces
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import town.robin.toadua.R

@Composable
fun ComposeEntryCard(
    modifier: Modifier = Modifier,
    mayCollapse: Boolean = true,
    data: EntryComposition,
    onTermChange: (term: String) -> Unit,
    onDefinitionChange: (definition: String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val term = data.term
    val definition = data.definition
    val initialTerm = data.initialTerm
    val initialDefinition = data.initialDefinition
    val busy = data.busy
    var termFocused by remember { mutableStateOf(false) }
    var definitionFocused by remember { mutableStateOf(false) }
    var definitionState by remember { mutableStateOf(TextFieldValue(definition)) }
    val expanded = !mayCollapse || termFocused || definitionFocused || term.isNotEmpty() || definition.isNotEmpty()

    if (definitionState.text != definition) definitionState = TextFieldValue(definition)

    FieldCard(
        modifier = modifier,
        value = term,
        onValueChange = onTermChange,
        onFocusChanged = { termFocused = it.isFocused },
        label = if (expanded) { -> Text(stringResource(R.string.term)) } else null,
        placeholder = if (expanded) null else { -> Text(stringResource(R.string.new_entry)) },
        icon = Icons.Outlined.Add,
        expanded = expanded,
        enabled = !busy,
    ) {
        Column {
            TextField(
                value = definitionState,
                onValueChange = { definitionState = it; onDefinitionChange(it.text) },
                label = { Text(stringResource(R.string.definition)) },
                colors = bareTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { definitionFocused = it.isFocused },
                enabled = !busy,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = cardPadding - 4.dp, end = cardPadding, bottom = cardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AnimatedVisibility(
                    visible = definitionFocused,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    OutlinedIconButton(
                        onClick = {
                            val newState = definitionState.copy(
                                text = definitionState.text.replaceRange(
                                    definitionState.selection.min,
                                    definitionState.selection.max,
                                    uiBlank.toString(),
                                ),
                                selection = TextRange(definitionState.selection.min + 1),
                            )
                            definitionState = newState
                            onDefinitionChange(newState.text)
                        },
                        enabled = !busy,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Workspaces,
                            contentDescription = stringResource(R.string.insert_blank),
                        )
                    }
                }
                ButtonRow {
                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus() // Allow the card to collapse
                            onCancel()
                        },
                        enabled = !busy,
                    ) { Text(stringResource(R.string.cancel)) }
                    Button(
                        onClick = {
                            focusManager.clearFocus() // Allow the card to collapse
                            onSubmit()
                        },
                        enabled = !busy &&
                            (term != initialTerm || definition != initialDefinition) &&
                            term.isNotBlank() && definition.isNotBlank(),
                    ) { Text(stringResource(R.string.submit)) }
                }
            }
        }
    }
}