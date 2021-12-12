package town.robin.toadua

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import town.robin.toadua.api.WelcomeRequest
import town.robin.toadua.databinding.ActivityMainBinding
import town.robin.toadua.databinding.NavHeaderBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHeaderBinding: NavHeaderBinding
    private lateinit var navController: NavController
    private val model: ToaduaViewModel by viewModels {
        ToaduaViewModel.Factory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        navHeaderBinding = NavHeaderBinding.bind(binding.navDrawer.getHeaderView(0))

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(model.loggedIn, model.prefs.username, model.serverName) { loggedIn, username, serverName ->
                    Triple(loggedIn, username, serverName)
                }.collect { (loggedIn, username, serverName) ->
                    navHeaderBinding.apply {
                        if (loggedIn) {
                            this.username.visibility = View.VISIBLE
                            server.visibility = View.VISIBLE
                            authStatus.setText(R.string.logged_in_as)
                            this.username.text = username
                            server.text = serverName
                        } else {
                            this.username.visibility = View.GONE
                            server.visibility = View.GONE
                            authStatus.text = getString(R.string.connected_to, serverName)
                        }
                    }
                }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.loggedIn.collect {
                    binding.navDrawer.menu.findItem(R.id.nav_log_out).isVisible = it
                    binding.navDrawer.menu.findItem(R.id.nav_sign_in).isVisible = !it
                }
            }
        }
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Keep the API flow alive so it reacts to server changes
                model.api.collect { }
            }
        }

        binding.navDrawer.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_search -> {
                    navController.popBackStack()
                    navController.navigate(R.id.search_fragment)
                    closeNavDrawer()
                    true
                }
                R.id.nav_glosser -> {
                    navController.popBackStack()
                    navController.navigate(R.id.gloss_fragment)
                    closeNavDrawer()
                    true
                }
                R.id.nav_language -> {
                    val input = EditText(this).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        ).apply {
                            setText(model.prefs.language.value)
                            val margin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                            marginStart = margin
                            marginEnd = margin
                        }
                    }
                    AlertDialog.Builder(this)
                        .setTitle(R.string.language)
                        .setView(FrameLayout(this).apply { addView(input) })
                        .setPositiveButton(R.string.confirm) { _, _ ->
                            model.prefs.language.value = input.text.toString()
                        }
                        .setNegativeButton(R.string.cancel) { _, _ -> }
                        .show().apply {
                            val button = getButton(AlertDialog.BUTTON_POSITIVE)
                            input.doOnTextChanged { text, _, _, _ ->
                                button.isEnabled = text?.isNotBlank() ?: false
                            }
                        }

                    input.postDelayed({ focusInput(input) },200)
                    true
                }
                R.id.nav_log_out -> {
                    model.logOut()
                    navController.popBackStack()
                    navController.navigate(R.id.auth_fragment)
                    closeNavDrawer()
                    true
                }
                R.id.nav_sign_in -> {
                    navController.popBackStack()
                    navController.navigate(R.id.auth_fragment)
                    closeNavDrawer()
                    true
                }
                R.id.nav_credits -> {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.credits)
                        .setMessage(R.string.credits_text)
                        .show()
                    true
                }
                else -> false
            }
        }

        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        navController = findNavController(R.id.nav_host)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.auth_fragment -> lockNavDrawer()
                else -> unlockNavDrawer()
            }
            when (destination.id) {
                R.id.search_fragment -> binding.navDrawer.menu.findItem(R.id.nav_search).isChecked = true
                R.id.gloss_fragment -> binding.navDrawer.menu.findItem(R.id.nav_glosser).isChecked = true
            }
        }

        if (model.loggedIn.value) {
            val glossQuery = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)
            if (glossQuery != null) {
                navController.popBackStack()
                navController.navigate(R.id.gloss_fragment, bundleOf("query" to glossQuery))
            }
            else {
                navController.popBackStack()
                navController.navigate(R.id.search_fragment)
            }

            lifecycleScope.launch(Dispatchers.IO) { verifyAuth() }
        }
    }

    // Verify the access token is still valid
    private suspend fun verifyAuth() {
        try {
            val welcome = model.api.value.welcome(WelcomeRequest(model.prefs.authToken.value!!))
            if (!(welcome.success && welcome.name == model.prefs.username.value)) {
                model.invalidateSession()
                withContext(Dispatchers.Main) {
                    navController.popBackStack()
                    navController.navigate(R.id.auth_fragment)
                }
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
                        navController.popBackStack()
                        navController.navigate(R.id.auth_fragment)
                    }
                    .show()
            }
        }
    }

    fun openNavDrawer() = binding.root.openDrawer(binding.navDrawer)
    private fun closeNavDrawer() = binding.root.closeDrawer(binding.navDrawer)
    private fun lockNavDrawer() = binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, binding.navDrawer)
    private fun unlockNavDrawer() = binding.root.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, binding.navDrawer)

    private fun focusInput(view: View) {
        view.requestFocus()
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}