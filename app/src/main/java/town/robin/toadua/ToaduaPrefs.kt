package town.robin.toadua

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

class BooleanPref(scope: CoroutineScope, key: String, default: Boolean, prefs: SharedPreferences) {
    private val flow = MutableStateFlow(prefs.getBoolean(key, default)).apply {
        scope.launch { collect { prefs.edit().putBoolean(key, it).apply() } }
    }
    operator fun getValue(thisRef: Any, property: KProperty<*>): MutableStateFlow<Boolean> = flow
}

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

fun SharedPreferences.boolean(scope: CoroutineScope, key: String, default: Boolean) = BooleanPref(scope, key, default, this)
fun SharedPreferences.string(scope: CoroutineScope, key: String, default: String) = StringPref(scope, key, default, this)
fun SharedPreferences.nullableString(scope: CoroutineScope, key: String, default: String?) = NullableStringPref(scope, key, default, this)

class ToaduaPrefs(scope: CoroutineScope, prefs: SharedPreferences) {
    val skipAuth by prefs.boolean(scope, "skip_auth", false)
    val server by prefs.string(scope, "server", "https://toadua.uakci.pl/")
    val authToken by prefs.nullableString(scope, "auth_token", null)
    val language by prefs.string(scope, "language", "en")
    val username by prefs.nullableString(scope, "username", null)
}