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
    var server: String by prefs.string("server", "http://[::1]:29138/")
    var authToken: String? by prefs.nullableString("auth_token")
    var language: String by prefs.string("language", "en")
    var username: String by prefs.string("username", "robintown") // TODO: un-hardcode
}