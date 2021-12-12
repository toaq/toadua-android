package town.robin.toadua

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import town.robin.toadua.databinding.FragmentAuthBinding
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AuthFragment : Fragment() {
    companion object {
        private const val ALERT_DIALOG_DELAY: Long = 200
    }

    private lateinit var binding: FragmentAuthBinding
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
            when (authType) {
                AuthType.SIGN_IN -> model::signIn
                AuthType.CREATE_ACCOUNT -> model::createAccount
            }.let { it(binding.usernameInput.text.toString(), binding.passwordInput.text.toString()) }
        }
        binding.createAccountButton.setOnClickListener {
            when (authType) {
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
        binding.changeServerButton.setOnClickListener {
            val input = EditText(requireContext()).apply {
                setText(activityModel.prefs.server.value)
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ).apply {
                    val margin = resources.getDimensionPixelSize(R.dimen.dialog_margin)
                    marginStart = margin
                    marginEnd = margin
                }
            }
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.server_address)
                .setView(FrameLayout(requireContext()).apply { addView(input) })
                .setPositiveButton(R.string.confirm) { _, _ ->
                    activityModel.prefs.server.value = input.text.toString()
                }
                .setNegativeButton(R.string.cancel) { _, _ -> }
                .show()

            input.postDelayed({ focusInput(input) }, ALERT_DIALOG_DELAY)
        }
        binding.skipButton.setOnClickListener {
            findNavController().navigate(R.id.auth_to_search)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.loading.collect {
                    binding.usernameInput.isEnabled = !it
                    binding.passwordInput.isEnabled = !it
                    binding.authLoadingIndicator.visibility = if (it) View.VISIBLE else View.GONE
                    binding.continueButton.isEnabled = !it
                    binding.createAccountButton.isEnabled = !it
                    binding.skipButton.isEnabled = !it
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.errors.collect { (type, message) ->
                    AlertDialog.Builder(requireContext())
                        .setMessage(getString(type.string, message ?: getString(R.string.cant_connect)))
                        .show()
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activityModel.serverName.collect {
                    binding.authServer.text = it
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                activityModel.loggedIn.collect {
                    if (it) findNavController().navigate(R.id.auth_to_search)
                }
            }
        }

        return binding.root
    }

    private fun focusInput(view: View) {
        view.requestFocus()
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}