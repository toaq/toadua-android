package town.robin.toadua

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import town.robin.toadua.api.WelcomeRequest
import town.robin.toadua.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val model: ToaduaViewModel by viewModels {
        ToaduaViewModel.Factory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        navController = findNavController(R.id.nav_host)

        if (model.loggedIn) {
            val glossQuery = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
            if (glossQuery != null)
                navController.navigate(R.id.gloss_fragment, bundleOf("query" to glossQuery))
            else
                navController.navigate(R.id.search_fragment)

            lifecycleScope.launch(Dispatchers.IO) { verifyAuth() }
        }
    }

    // Verify the access token is still valid
    private suspend fun verifyAuth() {
        try {
            val welcome = model.api.welcome(WelcomeRequest(model.prefs.authToken!!))
            if (!(welcome.success && welcome.name == model.prefs.username)) {
                model.invalidateSession()
                withContext(Dispatchers.Main) { navController.navigate(R.id.auth_fragment) }
            }
        } catch (t: Throwable) {
            withContext(Dispatchers.Main) {
                AlertDialog.Builder(this@MainActivity)
                    .setMessage(R.string.cant_connect)
                    .setPositiveButton(R.string.try_again) { _, _ ->
                        lifecycleScope.launch(Dispatchers.IO) { verifyAuth() }
                    }
                    .setNegativeButton(R.string.log_out) { _, _ ->
                        model.invalidateSession()
                        navController.navigate(R.id.auth_fragment)
                    }
                    .show()
            }
        }
    }
}