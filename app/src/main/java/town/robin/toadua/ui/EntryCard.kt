package town.robin.toadua.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import town.robin.toadua.R
import java.net.URI

private data class CommentGroup(val user: String, val contents: List<String>)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntryCard(
    modifier: Modifier = Modifier,
    entry: Entry,
    username: String?, // The logged in user
    onVote: (vote: Vote) -> Unit,
    getEntryLink: () -> URI,
    onPendingCommentChange: (comment: String) -> Unit,
    onCancelPendingComment: () -> Unit,
    onSubmitPendingComment: () -> Unit,
    onStartEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    onSubmitEdit: () -> Unit,
    onEditTermChange: (term: String) -> Unit,
    onEditDefinitionChange: (definition: String) -> Unit,
    onDelete: () -> Unit,
) {
    AnimatedContent(entry, contentKey = { it.pendingEdit == null }, label = "edit") { e ->
        if (e.pendingEdit == null) {
            val scope = rememberCoroutineScope()
            val clipboardManager = LocalClipboardManager.current
            var showMoreActions by remember { mutableStateOf(false) }
            var linkCopied by remember { mutableStateOf(false) }

            val hasCommentCards = e.comments.isNotEmpty() || e.pendingComment != null
            val normalRadius = 12.dp
            val smallRadius = 4.dp
            val cardBottomRadius = if (hasCommentCards) smallRadius else normalRadius
            val normalPadding = cardPadding
            val smallPadding = cardPadding / 2
            val smallNotePadding = cardPadding * 5 / 6
            val recessOutset = 4.dp
            val commentGap = 4.dp

            Column(modifier) {
                Card(
                    shape = RoundedCornerShape(
                        topStart = normalRadius,
                        topEnd = normalRadius,
                        bottomStart = cardBottomRadius,
                        bottomEnd = cardBottomRadius,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(
                            start = normalPadding - recessOutset,
                            top = normalPadding,
                            end = normalPadding - recessOutset,
                            bottom = if (hasCommentCards) smallPadding else normalPadding - recessOutset
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column(
                            Modifier.padding(horizontal = recessOutset),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(e.term, style = MaterialTheme.typography.titleLarge)
                            Text(e.definition, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            ProvideTextStyle(MaterialTheme.typography.titleSmall) {
                                RecessedSurface {
                                    Box(
                                        Modifier
                                            .padding(horizontal = 16.dp)
                                            .height(40.dp)
                                            .defaultMinSize(minWidth = 40.dp),
                                        contentAlignment = Alignment.CenterStart,
                                    ) {
                                        Text(e.user, Modifier.align(Alignment.Center))
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (e.score != 0) Text(
                                        if (e.score > 0)
                                            stringResource(R.string.positive_score, e.score)
                                        else
                                            stringResource(R.string.negative_score, -e.score),
                                        Modifier.padding(horizontal = 4.dp),
                                    )
                                    RecessedIconToggleButton(
                                        checked = e.vote == Vote.LIKE,
                                        onCheckedChange = {
                                            onVote(if (it) Vote.LIKE else Vote.NO_VOTE)
                                        },
                                        icon = if (e.vote == Vote.LIKE) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                                        contentDescription = stringResource(R.string.like),
                                    )
                                    RecessedIconToggleButton(
                                        checked = e.vote == Vote.DISLIKE,
                                        onCheckedChange = {
                                            onVote(if (it) Vote.DISLIKE else Vote.NO_VOTE)
                                        },
                                        icon = if (e.vote == Vote.DISLIKE) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                                        contentDescription = stringResource(R.string.dislike),
                                    )
                                    Box {
                                        RecessedIconButton(
                                            onClick = { showMoreActions = true },
                                            icon = Icons.Outlined.MoreVert,
                                            contentDescription = stringResource(R.string.more),
                                        )
                                        DropdownMenu(
                                            modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                                            expanded = showMoreActions,
                                            onDismissRequest = {
                                                showMoreActions = false; linkCopied = false
                                            },
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.comment)) },
                                                onClick = {
                                                    showMoreActions = false; onPendingCommentChange(
                                                    ""
                                                )
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Outlined.ChatBubbleOutline,
                                                        contentDescription = null
                                                    )
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = {
                                                    Crossfade(
                                                        targetState = linkCopied,
                                                        label = "copy_link"
                                                    ) {
                                                        Text(
                                                            if (it) stringResource(R.string.link_copied)
                                                            else stringResource(R.string.copy_link),
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    val link = getEntryLink().toString()
                                                    clipboardManager.setText(AnnotatedString(link))
                                                    linkCopied = true
                                                },
                                                leadingIcon = {
                                                    Crossfade(
                                                        targetState = linkCopied,
                                                        label = "copy_link_icon"
                                                    ) {
                                                        Icon(
                                                            imageVector =
                                                            if (it) Icons.Outlined.Check
                                                            else Icons.Outlined.Link,
                                                            contentDescription = null
                                                        )
                                                    }
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.edit)) },
                                                onClick = {
                                                    showMoreActions = false; onStartEdit()
                                                },
                                                leadingIcon = {
                                                    Icon(
                                                        imageVector = Icons.Outlined.Edit,
                                                        contentDescription = null
                                                    )
                                                },
                                            )
                                            if (e.user == username) {
                                                DropdownMenuItem(
                                                    text = { Text(stringResource(R.string.delete)) },
                                                    onClick = onDelete,
                                                    leadingIcon = {
                                                        Icon(
                                                            imageVector = Icons.Outlined.Delete,
                                                            contentDescription = null
                                                        )
                                                    },
                                                    colors = errorMenuItemColors(),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val groupedComments: List<CommentGroup> = remember(e.comments) {
                    e.comments.fold(listOf()) { groups, note ->
                        val lastGroup = groups.lastOrNull()
                        if (lastGroup?.user == note.user) {
                            groups.dropLast(1).plus(
                                lastGroup.copy(contents = lastGroup.contents.plus(note.content))
                            )
                        } else {
                            groups.plus(CommentGroup(note.user, listOf(note.content)))
                        }
                    }
                }

                for ((i, comments) in groupedComments.withIndex()) {
                    val isLast = i == groupedComments.size - 1 && e.pendingComment == null
                    val noteBottomRadius = if (isLast) normalRadius else smallRadius

                    Card(
                        modifier = Modifier
                            .padding(top = commentGap)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(
                            topStart = smallRadius,
                            topEnd = smallRadius,
                            bottomStart = noteBottomRadius,
                            bottomEnd = noteBottomRadius,
                        ),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                start = normalPadding,
                                top = smallNotePadding,
                                end = normalPadding,
                                bottom = smallNotePadding,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(comments.user, style = MaterialTheme.typography.titleSmall)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (content in comments.contents) {
                                    Text(content, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                if (e.pendingComment != null) {
                    val comment = e.pendingComment
                    val focusRequester = remember { FocusRequester() }
                    val bringIntoViewRequester = remember { BringIntoViewRequester() }
                    LaunchedEffect(Unit) { focusRequester.requestFocus() } // Auto-focus

                    Card(
                        modifier = Modifier
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .padding(top = commentGap)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(
                            topStart = smallRadius,
                            topEnd = smallRadius,
                            bottomStart = normalRadius,
                            bottomEnd = normalRadius,
                        ),
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            TextField(
                                value = comment.content,
                                onValueChange = onPendingCommentChange,
                                label = { Text(stringResource(R.string.new_comment)) },
                                colors = bareTextFieldColors(),
                                modifier = Modifier
                                    .focusRequester(focusRequester)
                                    .onFocusChanged {
                                        if (it.isFocused) scope.launch {
                                            bringIntoViewRequester.bringIntoView()
                                        }
                                    }
                                    .fillMaxWidth(),
                                enabled = !comment.busy,
                            )
                            ButtonRow(
                                modifier = Modifier.padding(
                                    start = cardPadding,
                                    end = cardPadding,
                                    bottom = smallNotePadding,
                                )
                            ) {
                                OutlinedButton(
                                    onClick = onCancelPendingComment,
                                    enabled = !comment.busy,
                                ) { Text(stringResource(R.string.cancel)) }
                                Button(
                                    onClick = onSubmitPendingComment,
                                    enabled = !comment.busy && comment.content.isNotBlank(),
                                ) { Text(stringResource(R.string.submit)) }
                            }
                        }
                    }
                }
            }
        } else {
            ComposeEntryCard(
                mayCollapse = false,
                data = e.pendingEdit,
                onTermChange = onEditTermChange,
                onDefinitionChange = onEditDefinitionChange,
                onSubmit = onSubmitEdit,
                onCancel = onCancelEdit,
            )
        }
    }
}