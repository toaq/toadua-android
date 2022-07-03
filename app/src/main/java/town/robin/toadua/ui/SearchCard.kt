package town.robin.toadua.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import town.robin.toadua.R
import town.robin.toadua.api.Arity
import town.robin.toadua.api.SortOrder

private data class Filter(
    val icon: ImageVector,
    val label: String,
    val key: Any?,
    val value: String?,
    val onSelect: () -> Unit,
    val onClear: () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchFilterChip(filter: Filter) {
    if (filter.value == null) SuggestionChip(
        onClick = filter.onSelect,
        label = { Text(filter.label) },
        icon = {
            Icon(
                imageVector = filter.icon,
                contentDescription = null,
                modifier = Modifier.size(SuggestionChipDefaults.IconSize),
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
        border = null,
    )
    else InputChip(
        selected = true,
        onClick = filter.onClear,
        label = { Text(filter.value) },
        leadingIcon = {
            Icon(
                imageVector = filter.icon,
                contentDescription = filter.label,
                modifier = Modifier.size(FilterChipDefaults.IconSize),
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Outlined.Clear,
                contentDescription = stringResource(R.string.clear_filter),
                modifier = Modifier.size(InputChipDefaults.IconSize),
            )
        },
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondary,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary,
            selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondary,
        ),
        border = null,
    )
}

@Composable
private fun sortOrderLabel(sortOrder: SortOrder?) = when (sortOrder) {
    SortOrder.HIGHEST -> stringResource(R.string.highest_score)
    SortOrder.LOWEST -> stringResource(R.string.lowest_score)
    SortOrder.NEWEST -> stringResource(R.string.newest)
    SortOrder.OLDEST -> stringResource(R.string.oldest)
    SortOrder.RANDOM -> stringResource(R.string.random)
    null -> stringResource(R.string.relevance)
}

@Composable
private fun arityLabel(arity: Arity) = when (arity) {
    Arity.NULLARY -> stringResource(R.string.nullary)
    Arity.UNARY -> stringResource(R.string.unary)
    Arity.BINARY -> stringResource(R.string.binary)
    Arity.TERNARY -> stringResource(R.string.ternary)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchCard(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (value: String) -> Unit,
    userFilter: String?,
    onUserFilterChange: (value: String?) -> Unit,
    idFilter: String?,
    onIdFilterChange: (value: String?) -> Unit,
    arityFilter: Arity?,
    onArityFilterChange: (value: Arity?) -> Unit,
    sortOrder: SortOrder?,
    onSortOrderChange: (value: SortOrder?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    var stagedUserFilter by remember { mutableStateOf("") }
    var editUserFilter by remember { mutableStateOf(false) }

    var stagedIdFilter by remember { mutableStateOf("") }
    var editIdFilter by remember { mutableStateOf(false) }

    var stagedArityFilter by remember { mutableStateOf<Arity?>(null) }
    var editArityFilter by remember { mutableStateOf(false) }

    val filters = listOf(
        Filter(
            icon = Icons.Outlined.Person,
            label = stringResource(R.string.author),
            key = "user",
            value = userFilter,
            onSelect = { editUserFilter = true },
            onClear = { onUserFilterChange(null); stagedUserFilter = "" },
        ),
        Filter(
            icon = Icons.Outlined.Tag,
            label = stringResource(R.string.id),
            key = "id",
            value = idFilter,
            onSelect = { editIdFilter = true },
            onClear = { onIdFilterChange(null); stagedIdFilter = "" },
        ),
        Filter(
            icon = Icons.Outlined.Workspaces,
            label = stringResource(R.string.arity),
            key = "arity",
            value = arityFilter?.let { arityLabel(it) },
            onSelect = { editArityFilter = true },
            onClear = { onArityFilterChange(null); stagedArityFilter = null },
        ),
    )

    if (editUserFilter) AlertDialog(
        onDismissRequest = { editUserFilter = false },
        confirmButton = {
            Button(onClick = {
                onUserFilterChange(stagedUserFilter.ifBlank { null })
                editUserFilter = false
            }) { Text(stringResource(R.string.set_filter)) }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                stagedUserFilter = userFilter ?: ""
                editUserFilter = false
            }) { Text(stringResource(R.string.cancel)) }
        },
        icon = { Icon(Icons.Outlined.Person, null) },
        title = { Text(stringResource(R.string.filter_by_author)) },
        text = {
            OutlinedTextField(
                value = stagedUserFilter,
                onValueChange = { stagedUserFilter = it },
                label = { Text(stringResource(R.string.username)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Ascii),
            )
        }
    )

    if (editIdFilter) AlertDialog(
        onDismissRequest = { editIdFilter = false },
        confirmButton = {
            Button(onClick = {
                onIdFilterChange(stagedIdFilter.ifBlank { null })
                editIdFilter = false
            }) { Text(stringResource(R.string.set_filter)) }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                stagedIdFilter = idFilter ?: ""
                editIdFilter = false
            }) { Text(stringResource(R.string.cancel)) }
        },
        icon = { Icon(Icons.Outlined.Tag, null) },
        title = { Text(stringResource(R.string.filter_by_id)) },
        text = {
            OutlinedTextField(
                value = stagedIdFilter,
                onValueChange = { stagedIdFilter = it },
                label = { Text(stringResource(R.string.entry_id)) },
                singleLine = true,
                leadingIcon = { Text("#", style = MaterialTheme.typography.bodyLarge) },
                keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Ascii),
            )
        }
    )

    if (editArityFilter) AlertDialog(
        onDismissRequest = { editArityFilter = false },
        confirmButton = {
            Button(onClick = {
                onArityFilterChange(stagedArityFilter)
                editArityFilter = false
            }) { Text(stringResource(R.string.set_filter)) }
        },
        dismissButton = {
            OutlinedButton(onClick = {
                stagedArityFilter = arityFilter
                editArityFilter = false
            }) { Text(stringResource(R.string.cancel)) }
        },
        icon = { Icon(Icons.Outlined.Workspaces, null) },
        title = { Text(stringResource(R.string.filter_by_arity)) },
        text = {
            Column(Modifier.selectableGroup()) {
                Arity.values().forEach { arity ->
                    val selected = arity == stagedArityFilter

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .selectable(
                                selected = selected,
                                onClick = { stagedArityFilter = if (selected) null else arity },
                                role = Role.RadioButton,
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = null,
                        )
                        Text(
                            text = arityLabel(arity),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
            }
        }
    )

    val chipsBeforeField = !expanded && (filters.any { it.value != null } || sortOrder != null)

    FieldCard(
        modifier = modifier,
        value = query,
        onValueChange = onQueryChange,
        placeholder = if (chipsBeforeField) null else ({ Text(stringResource(R.string.search)) }),
        icon = Icons.Outlined.Search,
        leadingIcon = if (chipsBeforeField) ({
            ChipGroup(Modifier.padding(start = 12.dp, end = 2.dp)) {
                filters.filter { it.value != null }.map {
                    key(it.key) { SearchFilterChip(it) }
                }
                if (sortOrder != null) {
                    RecessedFilterChip(
                        selected = true,
                        onClick = { onSortOrderChange(null) },
                        label = { Text(sortOrderLabel(sortOrder)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Clear,
                                contentDescription = stringResource(R.string.reset_sort_order),
                                modifier = Modifier.size(InputChipDefaults.IconSize),
                            )
                        },
                    )
                }
            }
        }) else null,
        trailingIcon = {
            Row {
                if (query.isNotEmpty()) key("clear") {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Outlined.Clear, stringResource(R.string.clear))
                    }
                }
                key("expand") {
                    IconButton(onClick = { expanded = !expanded }) {
                        if (expanded) Icon(Icons.Outlined.ExpandLess, stringResource(R.string.collapse))
                        else Icon(Icons.Outlined.ExpandMore, stringResource(R.string.expand))
                    }
                }
            }
        },
        expanded = expanded,
    ) {
        Column(
            modifier = Modifier.padding(cardPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChipGroup(
                label = { Text(stringResource(R.string.filter_by), style = MaterialTheme.typography.labelMedium) },
            ) {
                filters.map { SearchFilterChip(it) }
            }
            ChipGroup(
                label = { Text(stringResource(R.string.sort_by), style = MaterialTheme.typography.labelMedium) },
            ) {
                val sortChip = @Composable { key: SortOrder? ->
                    val selected = key == sortOrder
                    RecessedFilterChip(
                        selected = selected,
                        onClick = { onSortOrderChange(key) },
                        label = { Text(sortOrderLabel(key)) },
                        leadingIcon = {
                            if (selected) Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = stringResource(R.string.selected),
                                modifier = Modifier.size(FilterChipDefaults.IconSize),
                            )
                        },
                    )
                }

                sortChip(null)
                sortChip(SortOrder.NEWEST)
                sortChip(SortOrder.OLDEST)
                sortChip(SortOrder.HIGHEST)
                sortChip(SortOrder.LOWEST)
                sortChip(SortOrder.RANDOM)
            }
        }
    }
}