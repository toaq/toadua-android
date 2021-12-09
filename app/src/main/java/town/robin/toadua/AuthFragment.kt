package town.robin.toadua

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.navigation.fragment.findNavController
import town.robin.toadua.databinding.FragmentAuthBinding
import android.view.MenuItem
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AuthFragment : Fragment() {
    private lateinit var binding: FragmentAuthBinding
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private val activityModel: ToaduaViewModel by activityViewModels {
        ToaduaViewModel.Factory(requireContext())
    }
    private val model: AuthViewModel by viewModels {
        AuthViewModel.Factory(activityModel.api, activityModel.prefs)
    }

    private enum class AuthType { SIGN_IN, CREATE_ACCOUNT }
    private var authType = AuthType.SIGN_IN

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentAuthBinding.inflate(inflater, container, false)

        binding.continueButton.setOnClickListener {
            model.login(binding.usernameInput.text.toString(), binding.passwordInput.text.toString())
        }
        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.auth_to_search)
        }

        binding.createAccountButton.setOnClickListener {
            when(authType) {
                AuthType.SIGN_IN -> {
                    authType = AuthType.CREATE_ACCOUNT
                    binding.authTitle.text = getString(R.string.create_account)
                    binding.createAccountButton.text = getString(R.string.use_existing_account)
                }
                AuthType.CREATE_ACCOUNT -> {
                    authType = AuthType.SIGN_IN
                    binding.authTitle.text = getString(R.string.sign_in)
                    binding.createAccountButton.text = getString(R.string.create_account)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.loggedIn.collect {
                    if (it) findNavController().navigate(R.id.auth_to_search)
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        val drawerLayout = binding.myDrawerLayout
        actionBarDrawerToggle =
            ActionBarDrawerToggle(activity, drawerLayout, R.string.nav_open , R.string.nav_close)

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        actionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item)
}