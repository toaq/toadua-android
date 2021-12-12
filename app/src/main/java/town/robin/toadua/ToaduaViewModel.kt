package town.robin.toadua

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import town.robin.toadua.api.LogoutRequest
import town.robin.toadua.api.ToaduaService
import java.net.URI

class ToaduaViewModel(context: Context) : ViewModel() {
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ToaduaViewModel(context) as T
    }

    companion object {
        private const val PREFS = "prefs"
    }

    val prefs = ToaduaPrefs(viewModelScope, context.getSharedPreferences(PREFS, Context.MODE_PRIVATE))
    val api = prefs.server.map { ToaduaService.create(it) }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ToaduaService.create(prefs.server.value),
    )
    val loggedIn = prefs.authToken.map { it != null }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = prefs.authToken.value != null,
    )
    val serverName = prefs.server.map { URI(it).host }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = URI(prefs.server.value).host,
    )

    fun invalidateSession() {
        prefs.authToken.value = null
        prefs.username.value = null
        prefs.skipAuth.value = false
    }

    fun logOut() {
        val authToken = prefs.authToken.value!!
        invalidateSession()
        viewModelScope.launch {
            try {
                val logout = api.value.logout(LogoutRequest(authToken))
                if (!logout.success)
                    Log.w("logOut", "Failed to log out: ${logout.error}")
            } catch (t: Throwable) {
                Log.w("logOut", "Failed to log out", t)
            }
        }
    }
}