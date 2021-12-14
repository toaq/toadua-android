package town.robin.toadua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import town.robin.toadua.api.SearchRequest
import town.robin.toadua.api.ToaduaService
import java.text.Normalizer

class GlossViewModel(private val api: StateFlow<ToaduaService>, private val prefs: ToaduaPrefs) : ViewModel() {
    class Factory(private val api: StateFlow<ToaduaService>, private val prefs: ToaduaPrefs) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = GlossViewModel(api, prefs) as T
    }

    companion object {
        const val GLOSS_RATE_LIMIT: Long = 500
    }

    val query = MutableStateFlow("")
    val loading = MutableStateFlow(false)
    private val _errors = Channel<Pair<ErrorType, String?>>(Channel.RENDEZVOUS)
    val errors = _errors.receiveAsFlow()

    @FlowPreview @ExperimentalCoroutinesApi
    val results = combine(query, prefs.language) {
        query, language -> Pair(query, language)
    }.debounce(GLOSS_RATE_LIMIT).mapLatest { (query, language) ->
        if (query.isBlank()) {
            null
        } else {
            val terms = query.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }
            loading.value = true
            try {
                val search = api.value.search(SearchRequest.gloss(
                    prefs.authToken.value, language, terms,
                ))
                loading.value = false

                if (search.success && search.results != null) {
                    terms.map { term ->
                        val normalized = normalize(term)
                        Pair(term, search.results.find { normalized == normalize(it.head) })
                    }
                } else {
                    _errors.send(Pair(ErrorType.SEARCH, search.error))
                    listOf()
                }
            } catch (t: CancellationException) {
                throw t
            } catch (t: Throwable) {
                loading.value = false
                _errors.send(Pair(ErrorType.SEARCH, null))
                listOf()
            }
        }
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    private fun normalize(s: String) =
        Normalizer.normalize(s, Normalizer.Form.NFD)
            .replace("[^\\p{L}]+".toRegex(), "")
            .lowercase()
            .replace('ı', 'i')
}