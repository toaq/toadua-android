package town.robin.toadua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import town.robin.toadua.api.LoginRequest
import town.robin.toadua.api.RegisterRequest
import town.robin.toadua.api.ToaduaService

class AuthViewModel(val api: StateFlow<ToaduaService>, private val prefs: ToaduaPrefs) : ViewModel() {
    class Factory(private val api: StateFlow<ToaduaService>, private val prefs: ToaduaPrefs) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(api, prefs) as T
    }

    val loading = MutableStateFlow(false)
    private val _errors = Channel<Pair<ErrorType, String?>>(Channel.RENDEZVOUS)
    val errors = _errors.receiveAsFlow()

    fun signIn(username: String, password: String) {
        loading.value = true
        viewModelScope.launch {
            try {
                val login = api.value.login(LoginRequest(username, password))
                if (login.success && login.token != null) {
                    prefs.authToken.value = login.token
                    prefs.username.value = username
                } else {
                    loading.value = false
                    _errors.send(Pair(ErrorType.SIGN_IN, login.error))
                }
            } catch (t: Throwable) {
                loading.value = false
                _errors.send(Pair(ErrorType.SIGN_IN, null))
            }
        }
    }

    fun createAccount(username: String, password: String) {
        loading.value = true
        viewModelScope.launch {
            try {
                val register = api.value.register(RegisterRequest(username, password))
                if (register.success && register.token != null) {
                    prefs.authToken.value = register.token
                    prefs.username.value = username
                } else {
                    loading.value = false
                    _errors.send(Pair(ErrorType.CREATE_ACCOUNT, register.error))
                }
            } catch (t: Throwable) {
                loading.value = false
                _errors.send(Pair(ErrorType.CREATE_ACCOUNT, null))
            }
        }
    }
}