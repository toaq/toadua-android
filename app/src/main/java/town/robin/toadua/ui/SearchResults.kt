package town.robin.toadua.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import town.robin.toadua.R
import java.net.URI

fun searchResults(
    scope: GroupedLazyListScope,
    key: Any,
    entries: List<Entry>,
    username: String?,
    newEntry: EntryComposition,
    onNewEntryTermChange: (term: String) -> Unit,
    onNewEntryDefinitionChange: (definition: String) -> Unit,
    onSubmitNewEntry: () -> Unit,
    onVoteOnEntry: (id: String, vote: Vote) -> Unit,
    getEntryLink: (id: String) -> URI,
    onPendingCommentChange: (entry: Entry, comment: String) -> Unit,
    onCancelPendingComment: (entry: Entry) -> Unit,
    onSubmitPendingComment: (entry: Entry) -> Unit,
    onStartEdit: (entry: Entry) -> Unit,
    onCancelEdit: (entry: Entry) -> Unit,
    onSubmitEdit: (entry: Entry) -> Unit,
    onEditTermChange: (entry: Entry, term: String) -> Unit,
    onEditDefinitionChange: (entry: Entry, definition: String) -> Unit,
    onDeleteEntry: (entry: Entry) -> Unit,
) {
    scope.run {
        group(key, spacedBy = 24.dp) {
            when (entries.size) {
                0 -> listSection(
                    scope = this,
                    key = "noResults",
                    title = { Text(stringResource(R.string.no_results)) },
                ) {
                    item {
                        ComposeEntryCard(
                            data = newEntry,
                            onTermChange = onNewEntryTermChange,
                            onDefinitionChange = onNewEntryDefinitionChange,
                            onSubmit = onSubmitNewEntry,
                            onCancel = { onNewEntryTermChange(""); onNewEntryDefinitionChange("") },
                        )
                    }
                }
                else -> {
                    listSection(
                        scope = this,
                        key = "entries",
                        title = { Text(pluralStringResource(R.plurals.some_results, entries.size, entries.size)) },
                    ) {
                        items(entries, { it.id }) {
                            EntryCard(
                                entry = it,
                                username = username,
                                onVote = { v -> onVoteOnEntry(it.id, v) },
                                getEntryLink = { getEntryLink(it.id) },
                                onPendingCommentChange = { c -> onPendingCommentChange(it, c) },
                                onCancelPendingComment = { onCancelPendingComment(it) },
                                onSubmitPendingComment = { onSubmitPendingComment(it) },
                                onStartEdit = { onStartEdit(it) },
                                onCancelEdit = { onCancelEdit(it) },
                                onSubmitEdit = { onSubmitEdit(it) },
                                onEditTermChange = { t -> onEditTermChange(it, t) },
                                onEditDefinitionChange = { d -> onEditDefinitionChange(it, d) },
                                onDelete = { onDeleteEntry(it) },
                            )
                        }
                    }
                    listSection(
                        scope = this,
                        key = "newEntry",
                        title = { Text(stringResource(R.string.cant_find_question)) },
                    ) {
                        item {
                            ComposeEntryCard(
                                data = newEntry,
                                onTermChange = onNewEntryTermChange,
                                onDefinitionChange = onNewEntryDefinitionChange,
                                onSubmit = onSubmitNewEntry,
                                onCancel = { onNewEntryTermChange(""); onNewEntryDefinitionChange("") },
                            )
                        }
                    }
                }
            }
        }
    }
}