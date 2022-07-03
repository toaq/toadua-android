package town.robin.toadua.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import town.robin.toadua.R
import java.net.URI

fun wordOfTheDay(
    scope: GroupedLazyListScope,
    key: Any,
    onHide: () -> Unit,
    entry: Entry?, // null while loading
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
        title = { Text(stringResource(R.string.word_of_the_day)) },
        controls = {
            IconButton(onClick = onHide) {
                Icon(Icons.Outlined.VisibilityOff, stringResource(R.string.hide))
            }
        },
    ) {
        item {
            if (entry == null) {
                SkeletonEntryCard(Modifier.alpha(0.5f))
            } else {
                EntryCard(
                    entry = entry,
                    username = username,
                    onVote = { v -> onVoteOnEntry(entry.id, v) },
                    getEntryLink = { getEntryLink(entry.id) },
                    onPendingCommentChange = { c -> onPendingCommentChange(entry, c) },
                    onCancelPendingComment = { onCancelPendingComment(entry) },
                    onSubmitPendingComment = { onSubmitPendingComment(entry) },
                    onStartEdit = { onStartEdit(entry) },
                    onCancelEdit = { onCancelEdit(entry) },
                    onSubmitEdit = { onSubmitEdit(entry) },
                    onEditTermChange = { t -> onEditTermChange(entry, t) },
                    onEditDefinitionChange = { d -> onEditDefinitionChange(entry, d) },
                    onDelete = { onDeleteEntry(entry) },
                )
            }
        }
    }
}