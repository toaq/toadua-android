package town.robin.toadua

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

class StringPref(scope: CoroutineScope, key: String, default: String, prefs: SharedPreferences) {
    private val flow = MutableStateFlow(prefs.getString(key, default)!!).apply {
        scope.launch { collect { prefs.edit().putString(key, it).apply() } }
    }
    operator fun getValue(thisRef: Any, property: KProperty<*>): MutableStateFlow<String> = flow
}

class NullableStringPref(scope: CoroutineScope, key: String, default: String?, prefs: SharedPreferences) {
    private val flow = MutableStateFlow(prefs.getString(key, default)).apply {
        scope.launch { collect { prefs.edit().putString(key, it).apply() } }
    }
    operator fun getValue(thisRef: Any, property: KProperty<*>): MutableStateFlow<String?> = flow
}

fun SharedPreferences.string(scope: CoroutineScope, key: String, default: String) = StringPref(scope, key, default, this)
fun SharedPreferences.nullableString(scope: CoroutineScope, key: String, default: String?) = NullableStringPref(scope, key, default, this)

class ToaduaPrefs(scope: CoroutineScope, prefs: SharedPreferences) {
    companion object {
        private const val DEFAULT_SERVER = "https://toadua.uakci.pl/"
        private const val DEFAULT_LANGUAGE = "en"
    }

    val server by prefs.string(scope, "server", DEFAULT_SERVER)
    val authToken by prefs.nullableString(scope, "auth_token", null)
    val language by prefs.string(scope, "language", DEFAULT_LANGUAGE)
    val username by prefs.nullableString(scope, "username", null)
}