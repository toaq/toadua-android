package town.robin.toadua.ui

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import town.robin.toadua.R
import java.net.URI

fun recentlyAdded(
    scope: GroupedLazyListScope,
    key: Any,
    entries: List<Entry>?, // null while loading
    username: String?,
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
    listSection(
        scope = scope,
        key = key,
        title = { Text(stringResource(R.string.recently_added)) },
    ) {
        if (entries == null) {
            items(3) { SkeletonEntryCard(Modifier.alpha(0.5f)) }
        } else {
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
    }
}