package town.robin.toadua.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import town.robin.toadua.R
import town.robin.toadua.ui.theme.ToaduaTheme

private data class MainContent(val searching: Boolean, val results: List<Entry>?)

private enum class MainContentKey { LANDING, LOADING_RESULTS, RESULTS }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Toadua(viewModel: ToaduaViewModel) {
    val language by viewModel.language.collectAsStateWithLifecycle()
    val languages by viewModel.languages.collectAsStateWithLifecycle()
    val username by viewModel.username.collectAsStateWithLifecycle()
    val pendingUsername by viewModel.pendingUsername.collectAsStateWithLifecycle()
    val pendingPassword by viewModel.pendingPassword.collectAsStateWithLifecycle()
    val pendingAntiSpamAnswer by viewModel.pendingAntiSpamAnswer.collectAsStateWithLifecycle()
    val createAccountState by viewModel.createAccountState.collectAsStateWithLifecycle()
    val signInState by viewModel.signInState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val searching by viewModel.searching.collectAsStateWithLifecycle()
    val wordOfTheDay by viewModel.wordOfTheDay.collectAsStateWithLifecycle()
    val showWordOfTheDay by viewModel.showWordOfTheDay.collectAsStateWithLifecycle()
    val recentlyAdded by viewModel.recentlyAdded.collectAsStateWithLifecycle()
    val newEntry by viewModel.newEntry.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources

    val deleteEntry = { entry: Entry ->
        viewModel.stageDeletion(entry.id)
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = resources.getString(R.string.deleted_entry, entry.term),
                actionLabel = resources.getString(R.string.undo),
                duration = SnackbarDuration.Short,
            )
            when (result) {
                SnackbarResult.Dismissed -> viewModel.commitDeletion()
                SnackbarResult.ActionPerformed -> viewModel.unstageDeletion(entry.id)
            }
        }
        Unit
    }

    LaunchedEffect(Unit) {
        viewModel.errors.collect {
            snackbarHostState.showSnackbar(
                when (it) {
                    ErrorType.SEARCH -> resources.getString(R.string.search_error)
                    ErrorType.CREATE -> resources.getString(R.string.create_entry_error)
                    ErrorType.DELETE -> resources.getString(R.string.delete_entry_error)
                    ErrorType.RESTORE -> resources.getString(R.string.recover_entry_error)
                    ErrorType.VOTE -> resources.getString(R.string.vote_error)
                    ErrorType.COMMENT -> resources.getString(R.string.comment_error)
                }
            )
        }
    }

    ToaduaTheme {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            if (createAccountState != null) CreateAccountDialog(
                onDismiss = viewModel::hideCreateAccount,
                onSubmit = viewModel::createAccount,
                username = pendingUsername,
                onUsernameChange = viewModel::setPendingUsername,
                password = pendingPassword,
                onPasswordChange = viewModel::setPendingPassword,
                antiSpamAnswer = pendingAntiSpamAnswer,
                onAntiSpamAnswerChange = viewModel::setPendingAntiSpamAnswer,
                state = createAccountState!!,
            )

            if (signInState != null) SignInDialog(
                onDismiss = viewModel::hideSignIn,
                onSubmit = viewModel::signIn,
                username = pendingUsername,
                onUsernameChange = viewModel::setPendingUsername,
                password = pendingPassword,
                onPasswordChange = viewModel::setPendingPassword,
                state = signInState!!,
            )

            Surface(
                modifier = Modifier
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize(),
                color = MaterialTheme.colorScheme.background,
            ) {
                val layoutDirection = LocalLayoutDirection.current

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = innerPadding.calculateStartPadding(layoutDirection),
                            top = innerPadding.calculateTopPadding(),
                            end = innerPadding.calculateEndPadding(layoutDirection),
                        )
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        LanguageMenu(
                            language = language,
                            languages = languages,
                            onSelectLanguage = viewModel::selectLanguage,
                        )
                        UserMenu(
                            username = username,
                            onShowCreateAccount = viewModel::showCreateAccount,
                            onShowSignIn = viewModel::showSignIn,
                            onSignOut = viewModel::signOut,
                        )
                    }
                    SearchCard(
                        modifier = Modifier.fillMaxWidth(),
                        query = query.query,
                        onQueryChange = viewModel::setQuery,
                        userFilter = query.userFilter,
                        onUserFilterChange = viewModel::setUserFilter,
                        idFilter = query.idFilter,
                        onIdFilterChange = viewModel::setIdFilter,
                        arityFilter = query.arityFilter,
                        onArityFilterChange = viewModel::setArityFilter,
                        sortOrder = query.sortOrder,
                        onSortOrderChange = viewModel::setSortOrder,
                    )

                    val slideDistance = with(LocalDensity.current) { 16.dp.toPx().toInt() }

                    AnimatedContent(
                        targetState = remember(searching, results) {
                            MainContent(
                                searching,
                                results
                            )
                        },
                        contentKey = { content ->
                            if (content.searching) MainContentKey.LOADING_RESULTS
                            else if (results == null) MainContentKey.LANDING
                            else MainContentKey.RESULTS
                        },
                        transitionSpec = {
                            if (
                                (!targetState.searching && targetState.results == null)
                                == (!initialState.searching && initialState.results == null)
                            ) {
                                crossfade
                            } else {
                                // Disable bounds clipping to make the most of the slide effect
                                slideFadeAndReplace(slideDistance).using(SizeTransform(clip = false))
                            }
                        },
                    ) { content ->
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (!content.searching && content.results == null)
                                ComposeEntryCard(
                                    Modifier.fillMaxWidth(),
                                    data = newEntry,
                                    onTermChange = viewModel::setNewEntryTerm,
                                    onDefinitionChange = viewModel::setNewEntryDefinition,
                                    onSubmit = viewModel::submitNewEntry,
                                    onCancel = {
                                        viewModel.setNewEntryTerm("")
                                        viewModel.setNewEntryDefinition("")
                                    }
                                )

                            GroupedLazyColumn(
                                Modifier
                                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                    .imePadding(),
                                spacedBy = 16.dp,
                                userScrollEnabled = !content.searching,
                                contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 12.dp),
                            ) {
                                if (content.searching) {
                                    listSection(
                                        scope = this,
                                        key = "results",
                                        title = { SkeletonText(5, Modifier.alpha(0.5f)) },
                                    ) {
                                        items(2) { SkeletonEntryCard(Modifier.alpha(0.5f)) }
                                    }
                                } else if (content.results == null) {
                                    if (showWordOfTheDay) wordOfTheDay(
                                        scope = this,
                                        key = "word_of_the_day",
                                        onHide = viewModel::hideWordOfTheDay,
                                        entry = wordOfTheDay,
                                        username = username,
                                        onVoteOnEntry = viewModel::voteOnEntry,
                                        getEntryLink = viewModel::getEntryLink,
                                        onPendingCommentChange = viewModel::setPendingComment,
                                        onCancelPendingComment = viewModel::cancelPendingComment,
                                        onSubmitPendingComment = viewModel::submitPendingComment,
                                        onStartEdit = viewModel::startEdit,
                                        onCancelEdit = viewModel::cancelEdit,
                                        onSubmitEdit = viewModel::submitEdit,
                                        onEditTermChange = viewModel::setEditTerm,
                                        onEditDefinitionChange = viewModel::setEditDefinition,
                                        onDeleteEntry = deleteEntry,
                                    )
                                    recentlyAdded(
                                        scope = this,
                                        key = "recently_added",
                                        entries = recentlyAdded,
                                        username = username,
                                        onVoteOnEntry = viewModel::voteOnEntry,
                                        getEntryLink = viewModel::getEntryLink,
                                        onPendingCommentChange = viewModel::setPendingComment,
                                        onCancelPendingComment = viewModel::cancelPendingComment,
                                        onSubmitPendingComment = viewModel::submitPendingComment,
                                        onStartEdit = viewModel::startEdit,
                                        onCancelEdit = viewModel::cancelEdit,
                                        onSubmitEdit = viewModel::submitEdit,
                                        onEditTermChange = viewModel::setEditTerm,
                                        onEditDefinitionChange = viewModel::setEditDefinition,
                                        onDeleteEntry = deleteEntry,
                                    )
                                } else {
                                    searchResults(
                                        scope = this,
                                        key = "results",
                                        entries = content.results,
                                        username = username,
                                        newEntry = newEntry,
                                        onNewEntryTermChange = viewModel::setNewEntryTerm,
                                        onNewEntryDefinitionChange = viewModel::setNewEntryDefinition,
                                        onSubmitNewEntry = viewModel::submitNewEntry,
                                        onVoteOnEntry = viewModel::voteOnEntry,
                                        getEntryLink = viewModel::getEntryLink,
                                        onPendingCommentChange = viewModel::setPendingComment,
                                        onCancelPendingComment = viewModel::cancelPendingComment,
                                        onSubmitPendingComment = viewModel::submitPendingComment,
                                        onStartEdit = viewModel::startEdit,
                                        onCancelEdit = viewModel::cancelEdit,
                                        onSubmitEdit = viewModel::submitEdit,
                                        onEditTermChange = viewModel::setEditTerm,
                                        onEditDefinitionChange = viewModel::setEditDefinition,
                                        onDeleteEntry = deleteEntry,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}