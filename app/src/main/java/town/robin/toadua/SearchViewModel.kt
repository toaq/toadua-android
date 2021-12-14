package town.robin.toadua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import town.robin.toadua.api.*

class SearchViewModel(private val api: StateFlow<ToaduaService>, private val prefs: ToaduaPrefs) : ViewModel() {
    class Factory(private val api: StateFlow<ToaduaService>, private val prefs: ToaduaPrefs) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchViewModel(api, prefs) as T
    }

    companion object {
        private const val SEARCH_RATE_LIMIT: Long = 250
    }

    val query = MutableStateFlow("")
    val userFilter = MutableStateFlow("")
    val sortOrder = MutableStateFlow<SortOrder?>(null)

    val loading = MutableStateFlow(false)
    val createMode = MutableStateFlow(false)
    private val _errors = Channel<Pair<ErrorType, String?>>(Channel.RENDEZVOUS)
    val errors = _errors.receiveAsFlow()

    @FlowPreview @ExperimentalCoroutinesApi
    val results = combine(query, userFilter, sortOrder, prefs.language) { query, userFilter, sortOrder, language ->
        QueryParams(query, userFilter, sortOrder, language)
    }.debounce(SEARCH_RATE_LIMIT).mapLatest { (query, userFilter, sortOrder, language) ->
        if (createMode.value && (query.isNotEmpty() || userFilter.isNotEmpty()))
            createMode.value = false

        if (query.isBlank() && userFilter.isBlank()) {
            null
        } else {
            loading.value = true
            try {
                val search = api.value.search(
                    SearchRequest.search(
                        prefs.authToken.value,
                        language,
                        query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() },
                        if (userFilter.isBlank()) null else userFilter,
                        sortOrder,
                    )
                )

                if (search.success && search.results != null) {
                    search.results
                } else {
                    _errors.send(Pair(ErrorType.SEARCH, search.error))
                    mutableListOf()
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                _errors.send(Pair(ErrorType.SEARCH, null))
                mutableListOf()
            } finally {
                loading.value = false
            }
        }
    }
    val uiResults = MutableStateFlow<LiveList<Entry>?>(null)
    var resultsScope = viewModelScope + Job()

    fun cancelActions() {
        resultsScope.cancel()
        resultsScope = viewModelScope + Job()
    }

    fun createEntry(term: String, definition: String) {
        loading.value = true
        resultsScope.launch {
            try {
                val create = api.value.create(CreateRequest(
                    prefs.authToken.value!!, term, definition, prefs.language.value,
                ))
                if (create.success && create.entry != null) {
                    uiResults.value = LiveList(mutableListOf(create.entry), null, UpdateAction.ADD)
                } else {
                    _errors.send(Pair(ErrorType.CREATE, create.error))
                }
            } catch (t: Throwable) {
                _errors.send(Pair(ErrorType.CREATE, null))
            }
            loading.value = false
            createMode.value = false
        }
    }

    fun voteOnEntry(index: Int, vote: Int) {
        resultsScope.launch {
            val list = uiResults.value!!.list
            val entry = list[index]
            try {
                val response = api.value.vote(VoteRequest(prefs.authToken.value!!, entry.id, vote))
                if (response.success) {
                    entry.score += vote - entry.vote!!
                    entry.vote = vote
                    uiResults.value = LiveList(list, index, UpdateAction.MODIFY)
                } else {
                    _errors.send(Pair(ErrorType.VOTE, response.error))
                }
            } catch (t: Throwable) {
                _errors.send(Pair(ErrorType.VOTE, null))
            }
        }
    }

    fun commentOnEntry(index: Int, comment: String) {
        loading.value = true
        resultsScope.launch {
            val list = uiResults.value!!.list
            val entry = list[index]
            try {
                val note = api.value.note(NoteRequest(prefs.authToken.value!!, entry.id, comment))
                if (note.success) {
                    entry.notes.add(Note("", prefs.username.value!!, comment))
                    uiResults.value = LiveList(list, index, UpdateAction.MODIFY)
                } else {
                    _errors.send(Pair(ErrorType.COMMENT, note.error))
                }
            } catch (t: Throwable) {
                _errors.send(Pair(ErrorType.COMMENT, null))
            }
            loading.value = false
        }
    }

    fun deleteEntry(index: Int) {
        loading.value = true
        resultsScope.launch {
            val list = uiResults.value!!.list
            try {
                val remove = api.value.remove(RemoveRequest(prefs.authToken.value!!, list[index].id))
                if (remove.success) {
                    uiResults.value = LiveList(list.apply { removeAt(index) }, index, UpdateAction.REMOVE)
                } else {
                    _errors.send(Pair(ErrorType.DELETE, remove.error))
                }
            } catch (t: Throwable) {
                _errors.send(Pair(ErrorType.DELETE, null))
            }
            loading.value = false
        }
    }

    private data class QueryParams(
        val query: String,
        val userFilter: String,
        val sortOrder: SortOrder?,
        val language: String,
    )
}