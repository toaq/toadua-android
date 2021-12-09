package town.robin.toadua

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import town.robin.toadua.api.LoginRequest
import town.robin.toadua.api.RegisterRequest
import town.robin.toadua.api.ToaduaService

class AuthViewModel(var api: ToaduaService, private val prefs: ToaduaPrefs) : ViewModel() {
    class Factory(private val api: ToaduaService, private val prefs: ToaduaPrefs) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(api, prefs) as T
    }

    val loading = MutableStateFlow(false)
    val loggedIn = MutableStateFlow(false)

    fun signIn(username: String, password: String) {
        loading.value = true
        viewModelScope.launch {
            try {
                val login = api.login(LoginRequest(username, password))
                if (login.success && login.token != null) {
                    prefs.authToken = login.token
                    prefs.username = username
                    loggedIn.value = true
                } else {
                    loading.value = false
                    Log.w("signIn", "Failed to sign in: ${login.error}")
                }
            } catch (t: Throwable) {
                loading.value = false
                Log.w("signIn", "Failed to sign in", t)
            }
        }
    }

    fun createAccount(username: String, password: String) {
        loading.value = true
        viewModelScope.launch {
            try {
                val register = api.register(RegisterRequest(username, password))
                if (register.success && register.token != null) {
                    prefs.authToken = register.token
                    prefs.username = username
                    loggedIn.value = true
                } else {
                    loading.value = false
                    Log.w("createAccount", "Failed to create account: ${register.error}")
                }
            } catch (t: Throwable) {
                loading.value = false
                Log.w("createAccount", "Failed to create account", t)
            }
        }
    }
}