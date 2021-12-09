package town.robin.toadua

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import town.robin.toadua.api.WelcomeRequest
import town.robin.toadua.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
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

        if (model.loggedIn) {
            val navController = findNavController(R.id.nav_host)
            navController.navigate(R.id.search_fragment)

            // Verify that the user's token is still valid
            lifecycleScope.launch(Dispatchers.IO) {
                val welcome = model.api.welcome(WelcomeRequest(model.prefs.authToken!!))
                if (!(welcome.success && welcome.name != null)) {
                    model.prefs.apply {
                        authToken = null
                        username = null
                    }
                    withContext(Dispatchers.Main) { navController.navigate(R.id.auth_fragment) }
                }
            }
        }
    }
}