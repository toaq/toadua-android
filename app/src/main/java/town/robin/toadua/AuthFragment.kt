package town.robin.toadua

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
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
import androidx.core.widget.doOnTextChanged
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

        binding.usernameInput.doOnTextChanged { text, _, _, _ ->
            model.username.value = text?.toString() ?: ""
        }
        binding.passwordInput.doOnTextChanged { text, _, _, _ ->
            model.password.value = text?.toString() ?: ""
        }
        binding.continueButton.setOnClickListener {
            if (model.hasCredentials.value) {
                when (authType) {
                    AuthType.SIGN_IN -> model::signIn
                    AuthType.CREATE_ACCOUNT -> model::createAccount
                }(
                    binding.usernameInput.text.toString(),
                    binding.passwordInput.text.toString()
                )
            } else {
                findNavController().navigate(R.id.auth_to_search)
                activityModel.prefs.skipAuth.value = true
            }
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
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
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
                .show().apply {
                    val button = getButton(AlertDialog.BUTTON_POSITIVE)
                    input.doOnTextChanged { text, _, _, _ ->
                        button.isEnabled = text?.let { Patterns.WEB_URL.matcher(it).matches() } ?: false
                    }
                }

            input.postDelayed({ focusInput(input) }, ALERT_DIALOG_DELAY)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.hasCredentials.collect {
                    binding.continueButton.setText(if (it) R.string.cont else R.string.skip)
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                model.loading.collect {
                    binding.usernameInput.isEnabled = !it
                    binding.passwordInput.isEnabled = !it
                    binding.authLoadingIndicator.visibility = if (it) View.VISIBLE else View.GONE
                    binding.continueButton.isEnabled = !it
                    binding.createAccountButton.isEnabled = !it
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