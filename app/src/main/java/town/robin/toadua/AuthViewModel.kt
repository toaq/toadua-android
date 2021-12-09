package town.robin.toadua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import town.robin.toadua.api.LoginRequest
import town.robin.toadua.api.ToaduaService

class AuthViewModel(private val api: ToaduaService, private val prefs: ToaduaPrefs) : ViewModel() {
    class Factory(private val api: ToaduaService, private val prefs: ToaduaPrefs) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(api, prefs) as T
    }

    val loading = MutableStateFlow(false)
    val loggedIn = MutableStateFlow(prefs.authToken != null)

    fun login(username: String, password: String) {
        loading.value = true
        viewModelScope.launch {
            api.login(LoginRequest(username, password)).token?.let { prefs.authToken = it }
            loading.value = false
            loggedIn.value = true
        }
    }
}