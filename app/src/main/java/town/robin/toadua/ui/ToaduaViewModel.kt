package town.robin.toadua.ui

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.z4kn4fein.semver.constraints.toConstraint
import io.github.z4kn4fein.semver.satisfies
import io.github.z4kn4fein.semver.toVersionOrNull
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import town.robin.toadua.api.*
import java.net.URI
import java.util.Date
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val PREFS_KEY = "prefs"
private const val SERVER = "https://toadua.uakci.pl/"
private const val RECENTLY_ADDED_COUNT = 20
private val searchRateLimit = 250.toDuration(DurationUnit.MILLISECONDS)
private val illegalUsernameCharacters = Regex("[^a-zA-Z]")
private const val MAX_USERNAME_LENGTH = 64
private val correctAntiSpamAnswers =
    setOf("hoemai", "hoemaı", "jaffra", "solpa'i", "solpahi", "selpa'i", "selpahi")
private val supportedApiVersion = "^1.0.0".toConstraint()

private val defaultLanguages = mapOf(
    "en" to Language("en", "en", "English"),
    "toa" to Language("qtq-Latn", "toa", "Tóaqzu"),
    "jbo" to Language("jbo", "jbo", "la .lojban."),
    "tok" to Language("tok", "tok", "toki pona"),
    "ja" to Language("ja", "ja", "日本語"),
    "zh" to Language("zh-Hans", "zh", "官话"),
    "es" to Language("es", "es", "Español"),
    "fr" to Language("fr", "fr", "Français"),
    "de" to Language("de", "de", "Deutsch"),
    "pl" to Language("pl", "pl", "Polski"),
)

data class QueryParams(
    val query: String,
    val userFilter: String?,
    val idFilter: String?,
    val arityFilter: Arity?,
    val sortOrder: SortOrder?,
) {
    val triggersSearch: Boolean
        // Arity filter on its own shouldn't trigger a search
        get() = query.isNotBlank() || userFilter != null || idFilter != null
}

private val emptyQuery = QueryParams("", null, null, null, null)

private data class StateSummary(
    val results: List<Entry>?,
    val wordOfTheDay: Entry?,
    val recentlyAdded: List<Entry>?,
)

private data class Deletion(
    val id: String,
    val stateBefore: StateSummary,
    val stateAfter: StateSummary,
)

class ToaduaViewModel(context: Context) : ViewModel() {
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ToaduaViewModel(context) as T
    }

    private val prefs =
        ToaduaPrefs(viewModelScope, context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE))
    val language: StateFlow<String> = prefs.language
    val username: StateFlow<String?> = prefs.username

    private val api = ToaduaService.create(SERVER)

    private val _showIncompatibleVersionsWarning = MutableStateFlow(false)
    val showIncompatibleVersionsWarning: StateFlow<Boolean> = _showIncompatibleVersionsWarning

    fun hideIncompatibleVersionsWarning() {
        _showIncompatibleVersionsWarning.value = false
    }

    private fun checkApiAccess() {
        viewModelScope.launch {
            try {
                val welcome = api.welcome(WelcomeRequest(prefs.authToken.value))
                val version = welcome.version.toVersionOrNull()
                if (version != null && version satisfies supportedApiVersion) {
                    // Our username may have changed
                    prefs.username.value = welcome.name
                    // Also, the token could have expired
                    if (welcome.name == null) invalidateSession()
                } else {
                    Log.e("checkApiAccess", "Incompatible API version $version")
                    _showIncompatibleVersionsWarning.value = true
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                Log.e("checkApiAccess", "API not reachable", t)
            }
        }
    }

    private val _wordOfTheDay = MutableStateFlow<Entry?>(null)
    val wordOfTheDay: StateFlow<Entry?> = _wordOfTheDay
    val showWordOfTheDay: StateFlow<Boolean> = prefs.showWordOfTheDay

    fun hideWordOfTheDay() {
        prefs.showWordOfTheDay.value = false
    }

    private fun refreshWordOfTheDay() {
        val today = ToaduaPrefs.dayFormat.format(Date())

        if (prefs.wordOfTheDayDate.value == today) {
            // Don't bother updating what the user can't see
            if (showWordOfTheDay.value) {
                val entryId = prefs.wordOfTheDayId.value!!

                viewModelScope.launch {
                    try {
                        val search = api.search(
                            SearchRequest.search(
                                prefs.authToken.value,
                                ToaduaPrefs.defaultLanguage,
                                idFilter = entryId,
                            )
                        )
                        if (search.success) {
                            if (search.results?.size == 1) {
                                _wordOfTheDay.value = Entry(search.results.first())
                            } else {
                                // It's likely that the word was deleted
                                prefs.showWordOfTheDay.value = false
                            }
                        } else {
                            Log.e(
                                "refreshWordOfTheDay",
                                "Error getting entry #$entryId: ${search.error}"
                            )
                        }
                    } catch (t: CancellationException) {
                        throw t
                    } catch (t: Throwable) {
                        Log.e("refreshWordOfTheDay", "Error getting entry #$entryId", t)
                    }
                }
            }
        } else {
            // This is a new day; the new word should start off as visible
            prefs.showWordOfTheDay.value = true

            viewModelScope.launch {
                try {
                    val search = api.search(
                        SearchRequest.randomWord(
                            prefs.authToken.value,
                            ToaduaPrefs.defaultLanguage
                        )
                    )
                    if (search.success && search.results?.size == 1) {
                        val result = search.results.first()
                        prefs.wordOfTheDayDate.value = today
                        prefs.wordOfTheDayId.value = result.id
                        _wordOfTheDay.value = Entry(result)
                    } else {
                        Log.e("refreshWordOfTheDay", "Error selecting entry: ${search.error}")
                    }
                } catch (t: CancellationException) {
                    throw t
                } catch (t: Throwable) {
                    Log.e("refreshWordOfTheDay", "Error selecting entry", t)
                }
            }
        }
    }

    private val _recentlyAdded = MutableStateFlow<List<Entry>?>(null)
    val recentlyAdded: StateFlow<List<Entry>?> = _recentlyAdded

    private fun refreshRecentlyAdded() {
        viewModelScope.launch {
            try {
                val search = api.search(
                    SearchRequest.search(
                        prefs.authToken.value,
                        null,
                        sortOrder = SortOrder.NEWEST,
                        limit = RECENTLY_ADDED_COUNT,
                    )
                )
                if (search.success && !search.results.isNullOrEmpty()) {
                    _recentlyAdded.value = search.results.map { Entry(it) }
                } else {
                    Log.e("refreshRecentlyAdded", "Error getting entries: ${search.error}")
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                Log.e("refreshRecentlyAdded", "Error getting entries", t)
            }
        }
    }

    private val _pendingUsername = MutableStateFlow("")
    val pendingUsername: StateFlow<String> = _pendingUsername

    fun setPendingUsername(value: String) {
        if (_createAccountState.value == CreateAccountState.USERNAME_TAKEN)
            _createAccountState.value = CreateAccountState.WAITING_FOR_INPUT
        if (_signInState.value == SignInState.BAD_USERNAME)
            _signInState.value = SignInState.WAITING_FOR_INPUT
        _pendingUsername.value =
            illegalUsernameCharacters.replace(value, "").take(MAX_USERNAME_LENGTH)
    }

    private val _pendingPassword = MutableStateFlow("")
    val pendingPassword: StateFlow<String> = _pendingPassword

    fun setPendingPassword(value: String) {
        if (_signInState.value == SignInState.BAD_PASSWORD)
            _signInState.value = SignInState.WAITING_FOR_INPUT
        _pendingPassword.value = value
    }

    private val _pendingAntiSpamAnswer = MutableStateFlow("")
    val pendingAntiSpamAnswer: StateFlow<String> = _pendingAntiSpamAnswer

    fun setPendingAntiSpamAnswer(value: String) {
        if (_createAccountState.value == CreateAccountState.BAD_ANTI_SPAM_ANSWER)
            _createAccountState.value = CreateAccountState.WAITING_FOR_INPUT
        _pendingAntiSpamAnswer.value = value
    }

    // null = hidden
    private val _createAccountState = MutableStateFlow<CreateAccountState?>(null)
    val createAccountState: StateFlow<CreateAccountState?> = _createAccountState

    fun showCreateAccount() {
        _createAccountState.value = CreateAccountState.WAITING_FOR_INPUT
    }

    fun hideCreateAccount() {
        _pendingUsername.value = ""
        _pendingPassword.value = ""
        _pendingAntiSpamAnswer.value = ""
        _createAccountState.value = null
    }

    fun createAccount() {
        if (correctAntiSpamAnswers.contains(_pendingAntiSpamAnswer.value)) {
            _createAccountState.value = CreateAccountState.BUSY
            viewModelScope.launch {
                try {
                    val register = api.register(
                        RegisterRequest(
                            _pendingUsername.value,
                            _pendingPassword.value
                        )
                    )
                    if (register.success && register.token != null) {
                        prefs.authToken.value = register.token
                        prefs.username.value = _pendingUsername.value
                        hideCreateAccount()
                    } else {
                        _createAccountState.value = when (register.error) {
                            "already registered" -> CreateAccountState.USERNAME_TAKEN
                            else -> {
                                Log.e("createAccount", "Error creating account: ${register.error}")
                                CreateAccountState.UNKNOWN_ERROR
                            }
                        }
                    }
                } catch (t: CancellationException) {
                    throw t
                } catch (t: Throwable) {
                    Log.e("createAccount", "Error creating account", t)
                    _createAccountState.value = CreateAccountState.UNKNOWN_ERROR
                }
            }
        } else {
            _createAccountState.value = CreateAccountState.BAD_ANTI_SPAM_ANSWER
        }
    }

    // null = hidden
    private val _signInState = MutableStateFlow<SignInState?>(null)
    val signInState: StateFlow<SignInState?> = _signInState

    fun showSignIn() {
        _signInState.value = SignInState.WAITING_FOR_INPUT
    }

    fun hideSignIn() {
        _pendingUsername.value = ""
        _pendingPassword.value = ""
        _signInState.value = null
        onSignInCompleted = null
    }

    private var onSignInCompleted: (() -> Unit)? = null

    fun signIn() {
        _signInState.value = SignInState.BUSY
        viewModelScope.launch {
            try {
                val login =
                    api.login(LoginRequest(_pendingUsername.value, _pendingPassword.value))
                if (login.success && login.token != null) {
                    prefs.authToken.value = login.token
                    prefs.username.value = _pendingUsername.value

                    onSignInCompleted?.invoke()
                    hideSignIn()
                } else {
                    _signInState.value = when (login.error) {
                        "user not registered" -> SignInState.BAD_USERNAME
                        "password doesn't match" -> SignInState.BAD_PASSWORD
                        else -> {
                            Log.e("signIn", "Error signing in: ${login.error}")
                            SignInState.UNKNOWN_ERROR
                        }
                    }
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                Log.e("signIn", "Error signing in", t)
                _signInState.value = SignInState.UNKNOWN_ERROR
            }
        }
    }

    private fun ensureSignedIn(callback: () -> Unit) {
        if (username.value == null) {
            onSignInCompleted = callback
            showSignIn()
        } else {
            callback()
        }
    }

    private fun invalidateSession() {
        stagedDeletion = null
        prefs.authToken.value = null
        prefs.username.value = null
    }

    fun signOut() {
        viewModelScope.launch {
            commitDeletionInternal()
            val authToken = prefs.authToken.value!!
            invalidateSession()
            try {
                val logout = api.logout(LogoutRequest(authToken))
                if (!logout.success)
                    Log.w("signOut", "Error signing out: ${logout.error}")
            } catch (t: Throwable) {
                Log.w("signOut", "Error signing out", t)
            }
        }
    }

    private val _languages = MutableStateFlow(defaultLanguages)
    val languages: StateFlow<Map<String, Language>> = _languages

    fun selectLanguage(code: String) {
        val trimmedCode = code.trim()
        if (trimmedCode !in _languages.value)
            _languages.value =
                _languages.value.plus(trimmedCode to Language("en", trimmedCode, trimmedCode))
        prefs.language.value = trimmedCode
        // Set a matching locale
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(_languages.value[trimmedCode]?.locale))
    }

    // Ensure the language is present in the map and we start with a matching locale
    init {
        selectLanguage(language.value)
    }

    private val _query = MutableStateFlow(emptyQuery)
    val query: StateFlow<QueryParams> = _query

    private val requestQuery = Channel<QueryParams>(Channel.RENDEZVOUS)

    private fun updateQuery(transform: (QueryParams) -> QueryParams) {
        _query.value = transform(_query.value)
        viewModelScope.launch { requestQuery.send(_query.value) }
    }

    fun setQuery(value: String) {
        updateQuery { it.copy(query = value) }
    }

    fun setUserFilter(value: String?) {
        updateQuery { it.copy(userFilter = value) }
    }

    fun setIdFilter(value: String?) {
        updateQuery { it.copy(idFilter = value) }
    }

    fun setArityFilter(value: Arity?) {
        updateQuery { it.copy(arityFilter = value) }
    }

    fun setSortOrder(value: SortOrder?) {
        updateQuery { it.copy(sortOrder = value) }
    }

    fun showMyWords() {
        updateQuery {
            emptyQuery.copy(
                userFilter = prefs.username.value,
                sortOrder = SortOrder.NEWEST
            )
        }
    }

    private val _searching = MutableStateFlow(false)
    val searching: StateFlow<Boolean> = _searching

    private val _errors = Channel<ErrorType>(Channel.RENDEZVOUS)
    val errors = _errors.receiveAsFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private val requestResults =
        combine(requestQuery.receiveAsFlow(), language) { q, l -> Pair(q, l) }
            .debounce { (q) -> if (q.triggersSearch) searchRateLimit else 0.seconds }
            .mapLatest { (q, language) ->
                if (q.triggersSearch) {
                    _searching.value = true
                    try {
                        val search = api.search(
                            SearchRequest.search(
                                prefs.authToken.value,
                                // Ignore language if ID filter is set
                                if (q.idFilter == null) language else null,
                                q.query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() },
                                q.userFilter,
                                q.idFilter,
                                q.arityFilter,
                                q.sortOrder,
                            )
                        )

                        if (search.success && search.results != null) {
                            search.results.map { Entry(it) }
                        } else {
                            Log.e("search", "Error searching: ${search.error}")
                            _errors.send(ErrorType.SEARCH)
                            listOf()
                        }
                    } catch (t: CancellationException) {
                        throw t
                    } catch (t: Throwable) {
                        Log.e("search", "Error searching", t)
                        _errors.send(ErrorType.SEARCH)
                        listOf()
                    } finally {
                        _searching.value = false
                    }
                } else {
                    null
                }
            }

    private val syntheticResults = Channel<List<Entry>>(Channel.RENDEZVOUS)

    val results = merge(requestResults, syntheticResults.receiveAsFlow()).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null,
    )

    private suspend fun mapEntries(transform: (Entry) -> Entry) {
        results.value?.let { syntheticResults.send(it.map(transform)) }
        _wordOfTheDay.value = _wordOfTheDay.value?.let(transform)
        _recentlyAdded.value = _recentlyAdded.value?.map(transform)
    }

    private suspend fun updateEntriesWithId(id: String, transform: (Entry) -> Entry) {
        mapEntries { if (it.id == id) transform(it) else it }
    }

    private suspend fun updateEntry(old: Entry, new: Entry) {
        mapEntries { if (it === old) new else it }
    }

    private val _newEntry = MutableStateFlow(blankEntryComposition)
    val newEntry: StateFlow<EntryComposition> = _newEntry

    fun setNewEntryTerm(value: String) {
        _newEntry.value = _newEntry.value.copy(term = value)
    }

    fun setNewEntryDefinition(value: String) {
        _newEntry.value = _newEntry.value.copy(definition = value)
    }

    fun submitNewEntry() = ensureSignedIn {
        val data = _newEntry.value
        val authToken = prefs.authToken.value!!
        _newEntry.value = data.copy(busy = true)

        viewModelScope.launch {
            try {
                val create = api.create(
                    CreateRequest(
                        authToken,
                        data.term,
                        data.definition.replace(uiBlank, modelBlank),
                        prefs.language.value,
                    )
                )
                if (create.success && create.entry != null) {
                    // Populate the view with the returned entry
                    val entry = Entry(create.entry)
                    _newEntry.value = blankEntryComposition
                    _query.value = emptyQuery.copy(idFilter = create.entry.id)
                    syntheticResults.send(listOf(entry))

                    // Add it to the 'recently added' list as well
                    _recentlyAdded.value = _recentlyAdded.value?.prepend(entry)
                } else {
                    Log.e("createEntry", "Error creating entry: ${create.error}")
                    _errors.send(ErrorType.CREATE)
                    _newEntry.value = data
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                Log.e("createEntry", "Error creating entry", t)
                _errors.send(ErrorType.CREATE)
                _newEntry.value = data
            }
        }
    }

    fun startEdit(e: Entry) = viewModelScope.launch {
        updateEntry(e, e.copy(pendingEdit = EntryComposition(e.term, e.definition, false)))
    }

    fun cancelEdit(e: Entry) = viewModelScope.launch {
        updateEntry(e, e.copy(pendingEdit = null))
    }

    fun submitEdit(e: Entry) = ensureSignedIn {
        val authToken = prefs.authToken.value!!
        val edit = e.pendingEdit!!

        viewModelScope.launch {
            val busyEntry = e.copy(pendingEdit = edit.copy(busy = true))
            updateEntry(e, busyEntry)

            try {
                val create = api.create(
                    CreateRequest(
                        authToken,
                        edit.term,
                        edit.definition.replace(uiBlank, modelBlank),
                        prefs.language.value,
                    )
                )
                if (create.success && create.entry != null) {
                    // Populate the view with the returned entry
                    val editedEntry = Entry(create.entry)
                    _query.value = emptyQuery.copy(idFilter = create.entry.id)
                    syntheticResults.send(listOf(editedEntry))
                    updateEntry(busyEntry, editedEntry)

                    // Now delete the entry this was replacing
                    if (e.user == prefs.username.value) {
                        stageDeletion(e.id)
                        commitDeletionInternal()
                    }
                } else {
                    Log.e("submitEdit", "Error creating entry: ${create.error}")
                    _errors.send(ErrorType.CREATE)
                    updateEntry(busyEntry, e)
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                Log.e("submitEdit", "Error creating entry", t)
                _errors.send(ErrorType.CREATE)
                updateEntry(busyEntry, e)
            }
        }
    }

    fun setEditTerm(e: Entry, term: String) = viewModelScope.launch {
        updateEntry(e, e.copy(pendingEdit = e.pendingEdit!!.copy(term = term)))
    }

    fun setEditDefinition(e: Entry, definition: String) = viewModelScope.launch {
        updateEntry(e, e.copy(pendingEdit = e.pendingEdit!!.copy(definition = definition)))
    }

    fun setPendingComment(e: Entry, content: String) = viewModelScope.launch {
        updateEntry(e, e.copy(pendingComment = CommentComposition(content, false)))
    }

    fun cancelPendingComment(e: Entry) = viewModelScope.launch {
        updateEntry(e, e.copy(pendingComment = null))
    }

    fun submitPendingComment(e: Entry) = ensureSignedIn {
        val authToken = prefs.authToken.value!!
        val username = username.value!!
        val comment = e.pendingComment!!

        viewModelScope.launch {
            val busyEntry = e.copy(pendingComment = comment.copy(busy = true))
            updateEntry(e, busyEntry)

            try {
                val note = api.note(NoteRequest(authToken, e.id, comment.content))
                if (note.success) {
                    val c = Comment(username, comment.content)
                    updateEntriesWithId(e.id) {
                        it.copy(
                            comments = it.comments.plus(c),
                            pendingComment = null
                        )
                    }
                } else {
                    Log.e("commentOnEntry", "Error commenting: ${note.error}")
                    _errors.send(ErrorType.COMMENT)
                    updateEntry(busyEntry, e)
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                Log.e("commentOnEntry", "Error commenting", t)
                _errors.send(ErrorType.COMMENT)
                updateEntry(busyEntry, e)
            }
        }
    }

    fun voteOnEntry(id: String, vote: Vote) = ensureSignedIn {
        val authToken = prefs.authToken.value!!

        viewModelScope.launch {
            updateEntriesWithId(id) {
                it.copy(vote = vote, score = it.score + vote.score - it.vote.score)
            }

            try {
                val response = api.vote(VoteRequest(authToken, id, vote.score))
                if (!response.success) {
                    Log.e("voteOnEntry", "Error voting: ${response.error}")
                    _errors.send(ErrorType.VOTE)
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                Log.e("voteOnEntry", "Error voting", t)
                _errors.send(ErrorType.VOTE)
            }
        }
    }

    private var stagedDeletion: Deletion? = null

    fun stageDeletion(id: String) = ensureSignedIn {
        viewModelScope.launch {
            val resultsBefore = results.value
            val resultsAfter = resultsBefore?.filter { it.id != id }
            val wordOfTheDayBefore = _wordOfTheDay.value
            val wordOfTheDayAfter = if (wordOfTheDayBefore?.id == id) {
                prefs.showWordOfTheDay.value = false
                null
            } else wordOfTheDayBefore
            val recentlyAddedBefore = _recentlyAdded.value
            val recentlyAddedAfter = recentlyAddedBefore?.filter { it.id != id }

            stagedDeletion = Deletion(
                id = id,
                stateBefore = StateSummary(resultsBefore, wordOfTheDayBefore, recentlyAddedBefore),
                stateAfter = StateSummary(resultsAfter, wordOfTheDayAfter, recentlyAddedAfter),
            )

            if (resultsAfter != null) syntheticResults.send(resultsAfter)
            _wordOfTheDay.value = wordOfTheDayAfter
            _recentlyAdded.value = recentlyAddedAfter
        }
    }

    fun unstageDeletion(id: String) {
        viewModelScope.launch {
            val deletion = stagedDeletion
            if (deletion == null || id != deletion.id) {
                Log.e("unstageDeletion", "Entry $id can't be recovered anymore")
                _errors.send(ErrorType.RESTORE)
            } else {
                val before = deletion.stateBefore
                val after = deletion.stateAfter
                val wotd = _wordOfTheDay.value
                val recents = _recentlyAdded.value

                if (results.value == after.results && before.results != null) {
                    syntheticResults.send(before.results)
                }
                if (wotd == after.wordOfTheDay) {
                    _wordOfTheDay.value = before.wordOfTheDay
                    prefs.showWordOfTheDay.value = true
                }
                if (recents == after.recentlyAdded) _recentlyAdded.value = before.recentlyAdded

                stagedDeletion = null
            }
        }
    }

    private suspend fun commitDeletionInternal() {
        val deletion = stagedDeletion
        if (deletion != null) {
            try {
                val remove = api.remove(RemoveRequest(prefs.authToken.value!!, deletion.id))
                if (!remove.success) {
                    Log.e("flushStagedDeletion", "Error deleting entry: ${remove.error}")
                    _errors.send(ErrorType.DELETE)
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                Log.e("flushStagedDeletion", "Error deleting entry", t)
                _errors.send(ErrorType.DELETE)
            }
        }
    }

    fun commitDeletion() {
        viewModelScope.launch { commitDeletionInternal() }
    }

    fun getEntryLink(id: String): URI {
        val baseUrl = URI(SERVER)
        return URI(baseUrl.scheme, baseUrl.authority, baseUrl.path, "#$id")
    }

    fun onActivityPause() {
        commitDeletion()
    }

    fun onActivityResume() {
        checkApiAccess()
        refreshWordOfTheDay()
        refreshRecentlyAdded()
    }
}