package town.robin.toadua

import android.content.SharedPreferences
import kotlin.reflect.KProperty

class StringPref(private val key: String, private val default: String, private val prefs: SharedPreferences) {
    operator fun getValue(thisRef: Any, property: KProperty<*>): String = prefs.getString(key, default)!!
    operator fun setValue(thisRef: Any, property: KProperty<*>, value: String) = prefs.edit().putString(key, value).apply()
}

class NullableStringPref(private val key: String, private val prefs: SharedPreferences) {
    operator fun getValue(thisRef: Any, property: KProperty<*>): String? = prefs.getString(key, null)
    operator fun setValue(thisRef: Any, property: KProperty<*>, value: String?) = prefs.edit().putString(key, value).apply()
}

fun SharedPreferences.string(key: String, default: String) = StringPref(key, default, this)
fun SharedPreferences.nullableString(key: String) = NullableStringPref(key, this)

class ToaduaPrefs(prefs: SharedPreferences) {
    val server: String by prefs.string("server", "https://toadua.uakci.pl/")
    val authToken: String? by prefs.nullableString("auth_token")
    val language: String by prefs.string("language", "en")
}