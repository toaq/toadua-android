package town.robin.toadua

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import town.robin.toadua.api.LogoutRequest
import town.robin.toadua.api.ToaduaService
import java.net.URI

class ToaduaViewModel(context: Context) : ViewModel() {
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ToaduaViewModel(context) as T
    }

    val prefs = ToaduaPrefs(context.getSharedPreferences("prefs", Context.MODE_PRIVATE))
    var api = ToaduaService.create(prefs.server)
    val loggedIn get() = prefs.authToken != null
    val serverName get() = URI(prefs.server).host

    fun invalidateSession() {
        prefs.authToken = null
        prefs.username = null
    }

    fun logOut() {
        viewModelScope.launch {
            val logout = api.logout(LogoutRequest(prefs.authToken!!))
            if (!logout.success)
                Log.w("logOut", "Failed to log out: ${logout.error}")
            invalidateSession()
        }
    }
}