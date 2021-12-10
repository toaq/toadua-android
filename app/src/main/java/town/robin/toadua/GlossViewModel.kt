package town.robin.toadua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import town.robin.toadua.api.SearchRequest
import town.robin.toadua.api.ToaduaService
import java.text.Normalizer

class GlossViewModel(private val api: ToaduaService, private val prefs: ToaduaPrefs) : ViewModel() {
    class Factory(private val api: ToaduaService, private val prefs: ToaduaPrefs) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = GlossViewModel(api, prefs) as T
    }

    val query = MutableStateFlow("")
    val loading = MutableStateFlow(false)

    @FlowPreview @ExperimentalCoroutinesApi
    val results = query.debounce(500).mapLatest { query ->
        if (query.isBlank()) {
            null
        } else {
            val terms = query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            loading.value = true
            val search = api.search(SearchRequest.gloss(prefs.authToken!!, prefs.language, terms))
            loading.value = false

            terms.map { term ->
                val normalized = normalize(term)
                Pair(term, search.results!!.find { normalized == normalize(it.head) })
            }
        }
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    private fun normalize(s: String) =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .lowercase()
            .replace('ı', 'i')
}