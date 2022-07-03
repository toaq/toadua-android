package town.robin.toadua.ui

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

fun SharedPreferences.boolean(scope: CoroutineScope, key: String, default: Boolean) =
    MutableStateFlow(getBoolean(key, default)).apply {
        scope.launch { collect { edit().putBoolean(key, it).apply() } }
    }

fun SharedPreferences.string(scope: CoroutineScope, key: String, default: String) =
    MutableStateFlow(getString(key, default)!!).apply {
        scope.launch { collect { edit().putString(key, it).apply() } }
    }

fun SharedPreferences.nullableString(scope: CoroutineScope, key: String, default: String?) =
    MutableStateFlow(getString(key, default)).apply {
        scope.launch { collect { edit().putString(key, it).apply() } }
    }

class ToaduaPrefs(scope: CoroutineScope, prefs: SharedPreferences) {
    val authToken = prefs.nullableString(scope, "auth_token", null)
    val language = prefs.string(scope, "language", defaultLanguage)
    val username = prefs.nullableString(scope, "username", null)
    val wordOfTheDayId = prefs.nullableString(scope, "word_of_the_day_id", null)
    val wordOfTheDayDate = prefs.nullableString(scope, "word_of_the_day_date", null)
    val showWordOfTheDay = prefs.boolean(scope, "show_word_of_the_day", true)

    companion object {
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        const val defaultLanguage = "en"
    }
}