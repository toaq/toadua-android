package town.robin.toadua

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import town.robin.toadua.api.ToaduaService

class ToaduaViewModel(context: Context) : ViewModel() {
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ToaduaViewModel(context) as T
    }

    val prefs = ToaduaPrefs(context.getSharedPreferences("prefs", Context.MODE_PRIVATE))
    val api = ToaduaService.create(prefs.server)
    val loggedIn get() = prefs.authToken != null

    fun invalidateSession() {
        prefs.authToken = null
        prefs.username = null
    }
}