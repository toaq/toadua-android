package town.robin.toadua

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import town.robin.toadua.api.ToaduaService

class ToaduaViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ToaduaViewModel(context) as T
}

class ToaduaViewModel(context: Context) : ViewModel() {
    val prefs = ToaduaPrefs(context.getSharedPreferences("prefs", Context.MODE_PRIVATE))
    val api = ToaduaService.create(prefs.server)
    val username = "robintown" // TODO: implement auth and un-hardcode this
}