package town.robin.toadua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import town.robin.toadua.api.Entry
import town.robin.toadua.api.SearchRequest
import town.robin.toadua.api.ToaduaService

class SearchViewModelFactory(private val api: ToaduaService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = SearchViewModel(api) as T
}

class SearchViewModel(private val api: ToaduaService) : ViewModel() {
    val query = MutableStateFlow("")
    val loading = MutableStateFlow(false)

    val results: StateFlow<Array<Entry>> = query.debounce(250).mapLatest {
        val terms = it.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
        if (terms.isEmpty()) {
            arrayOf()
        } else {
            loading.value = true
            val response = api.search(SearchRequest(null, terms))
            loading.value = false
            response.results ?: arrayOf()
        }
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = WhileSubscribed(5000),
        initialValue = arrayOf()
    )
}