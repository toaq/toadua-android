package town.robin.toadua

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import town.robin.toadua.api.*

class SearchViewModel(private val api: StateFlow<ToaduaService>, private val prefs: ToaduaPrefs) : ViewModel() {
    class Factory(private val api: StateFlow<ToaduaService>, private val prefs: ToaduaPrefs) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchViewModel(api, prefs) as T
    }

    val query = MutableStateFlow("")
    val userFilter = MutableStateFlow("")
    val sortOrder = MutableStateFlow<SortOrder?>(null)

    val loading = MutableStateFlow(false)
    val createMode = MutableStateFlow(false)

    @FlowPreview @ExperimentalCoroutinesApi
    val results = combine(query, userFilter, sortOrder) { query, userFilter, sortOrder ->
        Triple(query, userFilter, sortOrder)
    }.debounce(250).mapLatest { (query, userFilter, sortOrder) ->
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
                        prefs.language.value,
                        query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() },
                        if (userFilter.isBlank()) null else userFilter,
                        sortOrder,
                    )
                )
                loading.value = false

                if (search.success && search.results != null) {
                    search.results
                } else {
                    Log.w("search", "Failed to search: ${search.error}")
                    mutableListOf()
                }
            } catch (t: Throwable) {
                loading.value = false
                Log.w("search", "Failed to search", t)
                mutableListOf()
            }
        }
    }
    val uiResults = MutableStateFlow<LiveList<Entry>?>(null)

    fun createEntry(term: String, definition: String) {
        viewModelScope.launch {
            loading.value = true
            try {
                val create = api.value.create(CreateRequest(
                    prefs.authToken.value!!, term, definition, prefs.language.value,
                ))
                if (create.success && create.entry != null) {
                    uiResults.value = LiveList(mutableListOf(create.entry), null, UpdateAction.ADD)
                } else {
                    Log.w("createEntry", "Failed to create entry: ${create.error}")
                }
            } catch (t: Throwable) {
                Log.w("createEntry", "Failed to create entry", t)
            }
            loading.value = false
            createMode.value = false
        }
    }

    fun voteOnEntry(index: Int, vote: Int) {
        viewModelScope.launch {
            val list = uiResults.value!!.list
            val entry = list[index]
            try {
                val response = api.value.vote(VoteRequest(prefs.authToken.value!!, entry.id, vote))
                if (response.success) {
                    entry.score += vote - entry.vote!!
                    entry.vote = vote
                    uiResults.value = LiveList(list, index, UpdateAction.MODIFY)
                } else {
                    Log.w("voteOnEntry", "Failed to vote on entry: ${response.error}")
                }
            } catch (t: Throwable) {
                Log.w("voteOnEntry", "Failed to vote on entry", t)
            }
        }
    }

    fun commentOnEntry(index: Int, comment: String) {
        viewModelScope.launch {
            val list = uiResults.value!!.list
            val entry = list[index]
            loading.value = true
            try {
                val note = api.value.note(NoteRequest(prefs.authToken.value!!, entry.id, comment))
                if (note.success) {
                    entry.notes.add(Note("", prefs.username.value!!, comment))
                    uiResults.value = LiveList(list, index, UpdateAction.MODIFY)
                } else {
                    Log.w("commentOnEntry", "Failed to comment on entry: ${note.error}")
                }
            } catch (t: Throwable) {
                Log.w("commentOnEntry", "Failed to comment on entry", t)
            }
            loading.value = false
        }
    }

    fun deleteEntry(index: Int) {
        viewModelScope.launch {
            val list = uiResults.value!!.list
            loading.value = true
            try {
                val remove = api.value.remove(RemoveRequest(prefs.authToken.value!!, list[index].id))
                if (remove.success) {
                    uiResults.value = LiveList(list.apply { removeAt(index) }, index, UpdateAction.REMOVE)
                } else {
                    Log.w("deleteEntry", "Failed to delete entry: ${remove.error}")
                }
            } catch (t: Throwable) {
                Log.w("deleteEntry", "Failed to delete entry", t)
            }
            loading.value = false
        }
    }
}