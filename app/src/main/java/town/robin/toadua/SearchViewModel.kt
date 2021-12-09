package town.robin.toadua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import town.robin.toadua.api.*

class SearchViewModel(private val api: ToaduaService, private val prefs: ToaduaPrefs) : ViewModel() {
    class Factory(private val api: ToaduaService, private val prefs: ToaduaPrefs) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchViewModel(api, prefs) as T
    }

    val query = MutableStateFlow("")
    val loading = MutableStateFlow(false)
    val createMode = MutableStateFlow(false)

    @ExperimentalCoroutinesApi @FlowPreview
    val results: StateFlow<MutableList<Entry>> = query.debounce(250).mapLatest { query ->
        if (createMode.value && query.isNotEmpty()) createMode.value = false
        val terms = query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (terms.isEmpty()) {
            mutableListOf()
        } else {
            loading.value = true
            val response = api.search(SearchRequest(prefs.authToken, terms))
            loading.value = false
            response.results ?: mutableListOf()
        }
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = mutableListOf()
    )
    val uiResults = MutableStateFlow(LiveList<Entry>(mutableListOf(), null, UpdateAction.ADD))

    fun createEntry(term: String, definition: String) {
        viewModelScope.launch {
            val create = api.create(CreateRequest(prefs.authToken!!, term, definition, prefs.language))
            uiResults.value = LiveList(mutableListOf(create.entry!!), null, UpdateAction.ADD)
            createMode.value = false
        }
    }

    fun voteOnEntry(index: Int, vote: Int) {
        viewModelScope.launch {
            val list = uiResults.value.list
            val entry = list[index]
            entry.score += vote - entry.vote!!
            entry.vote = vote
            uiResults.value = LiveList(list, index, UpdateAction.MODIFY)
            api.vote(VoteRequest(prefs.authToken!!, entry.id, vote))
        }
    }

    fun commentOnEntry(index: Int, comment: String) {
        viewModelScope.launch {
            val list = uiResults.value.list
            val entry = list[index]
            loading.value = true
            api.note(NoteRequest(prefs.authToken!!, entry.id, comment))
            loading.value = false
            entry.notes.add(Note("", prefs.username!!, comment))
            uiResults.value = LiveList(list, index, UpdateAction.MODIFY)
        }
    }

    fun deleteEntry(index: Int) {
        viewModelScope.launch {
            val list = uiResults.value.list
            loading.value = true
            api.remove(RemoveRequest(prefs.authToken!!, list[index].id))
            loading.value = false
            uiResults.value = LiveList(list.apply { removeAt(index) }, index, UpdateAction.REMOVE)
        }
    }
}